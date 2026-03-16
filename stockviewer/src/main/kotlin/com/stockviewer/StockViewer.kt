package com.stockviewer

import java.awt.*
import java.awt.geom.GeneralPath
import java.awt.geom.Line2D
import java.awt.geom.RoundRectangle2D
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.math.abs
import kotlin.math.min

// ── Colour palette ──────────────────────────────────────────────────────────
object Theme {
    val BG         = Color(10,  12,  20)
    val PANEL      = Color(16,  20,  34)
    val CARD       = Color(22,  28,  45)
    val BORDER     = Color(40,  50,  80)
    val ACCENT     = Color(0,   210, 150)
    val RED        = Color(255, 80,  100)
    val TEXT       = Color(230, 235, 255)
    val TEXT_DIM   = Color(120, 135, 175)
    val GRID       = Color(30,  38,  62)
    val CANDLE_UP  = Color(0,   210, 150)
    val CANDLE_DN  = Color(255, 80,  100)
}

// ── Data model ───────────────────────────────────────────────────────────────
data class Candle(
    val date: String,
    val open: Double,
    val high: Double,
    val low:  Double,
    val close: Double,
    val volume: Long
)

// ── Alpha Vantage API fetcher ─────────────────────────────────────────────────
object AlphaVantage {
    // Free key — limited to 25 req/day. Replace with your own from alphavantage.co
    private const val API_KEY = "demo"
    private val client = HttpClient.newHttpClient()

    fun fetchDaily(symbol: String): List<Candle> {
        val url = "https://www.alphavantage.co/query" +
            "?function=TIME_SERIES_DAILY_ADJUSTED" +
            "&symbol=${symbol.uppercase()}" +
            "&outputsize=full" +
            "&apikey=$API_KEY"
        return try {
            val req = HttpRequest.newBuilder(URI.create(url)).GET().build()
            val body = client.send(req, HttpResponse.BodyHandlers.ofString()).body()
            parseAlphaVantage(body)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseAlphaVantage(json: String): List<Candle> {
        val candles = mutableListOf<Candle>()
        // Minimal hand-rolled JSON parser (no external deps needed)
        val seriesKey = "\"Time Series (Daily)\""
        val seriesIdx = json.indexOf(seriesKey)
        if (seriesIdx < 0) return emptyList()
        val start = json.indexOf('{', seriesIdx + seriesKey.length)
        if (start < 0) return emptyList()

        var i = start + 1
        val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        while (i < json.length) {
            // find date key "YYYY-MM-DD"
            val dateStart = json.indexOf('"', i)
            if (dateStart < 0) break
            val dateEnd   = json.indexOf('"', dateStart + 1)
            val dateStr   = json.substring(dateStart + 1, dateEnd)
            if (!dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) break

            fun extractField(field: String): Double {
                val idx = json.indexOf("\"$field\"", dateEnd)
                if (idx < 0) return 0.0
                val colon = json.indexOf(':', idx)
                val valStart = json.indexOfFirst { it != ' ' && it != '"' }.let {
                    var k = colon + 1
                    while (k < json.length && (json[k] == ' ' || json[k] == '"')) k++
                    k
                }
                var k = colon + 1
                while (k < json.length && (json[k] == ' ' || json[k] == '"')) k++
                var end = k
                while (end < json.length && json[end].let { c -> c.isDigit() || c == '.' || c == '-' }) end++
                return json.substring(k, end).toDoubleOrNull() ?: 0.0
            }

            val open   = extractField("1. open")
            val high   = extractField("2. high")
            val low    = extractField("3. low")
            val close  = extractField("4. close")
            val volume = extractField("6. volume").toLong()

            candles.add(Candle(dateStr, open, high, low, close, volume))
            // advance past this entry's closing brace
            val entryEnd = json.indexOf('}', dateEnd)
            i = entryEnd + 1
        }
        return candles.sortedBy { it.date }
    }
}

// ── Chart panel ───────────────────────────────────────────────────────────────
class ChartPanel(private var candles: List<Candle>) : JPanel() {

    enum class ChartType { CANDLESTICK, LINE, AREA }

    var chartType: ChartType = ChartType.CANDLESTICK
    private val pad = Insets(40, 70, 60, 30)

    init {
        background = Theme.BG
        preferredSize = Dimension(900, 480)
    }

    fun setData(data: List<Candle>) { candles = data; repaint() }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)

        val cw = width  - pad.left - pad.right
        val ch = height - pad.top  - pad.bottom

        if (candles.isEmpty()) {
            g2.color = Theme.TEXT_DIM
            g2.font  = Font("Monospaced", Font.PLAIN, 14)
            val msg = "No data — enter a symbol and press Load"
            val fm  = g2.fontMetrics
            g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2)
            return
        }

        val minPrice = candles.minOf { it.low }
        val maxPrice = candles.maxOf { it.high }
        val priceRange = maxPrice - minPrice

        fun xOf(idx: Int) = pad.left + idx.toDouble() / (candles.size - 1).coerceAtLeast(1) * cw
        fun yOf(p: Double) = pad.top + ch - (p - minPrice) / priceRange * ch

        // Grid
        g2.stroke = BasicStroke(1f)
        g2.color  = Theme.GRID
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = pad.top + i.toDouble() / gridLines * ch
            g2.drawLine(pad.left, y.toInt(), pad.left + cw, y.toInt())
            val price = maxPrice - i.toDouble() / gridLines * priceRange
            g2.color = Theme.TEXT_DIM
            g2.font  = Font("Monospaced", Font.PLAIN, 11)
            g2.drawString("%.2f".format(price), 4, y.toInt() + 4)
            g2.color = Theme.GRID
        }

        // X-axis labels (show ~8 dates)
        val step = (candles.size / 8).coerceAtLeast(1)
        g2.color = Theme.TEXT_DIM
        g2.font  = Font("Monospaced", Font.PLAIN, 10)
        for (i in candles.indices step step) {
            val x  = xOf(i).toInt()
            val lbl = candles[i].date.substring(5) // MM-DD
            val fm  = g2.fontMetrics
            g2.drawString(lbl, x - fm.stringWidth(lbl) / 2, height - 10)
        }

        when (chartType) {
            ChartType.CANDLESTICK -> drawCandlestick(g2, ::xOf, ::yOf, cw)
            ChartType.LINE        -> drawLine(g2, ::xOf, ::yOf)
            ChartType.AREA        -> drawArea(g2, ::xOf, ::yOf, ch)
        }

        // Border frame
        g2.color  = Theme.BORDER
        g2.stroke = BasicStroke(1f)
        g2.drawRect(pad.left, pad.top, cw, ch)
    }

    private fun drawCandlestick(g2: Graphics2D, xOf: (Int) -> Double, yOf: (Double) -> Double, cw: Int) {
        val candleW = ((cw.toDouble() / candles.size) * 0.6).coerceIn(1.0, 16.0)
        for ((i, c) in candles.withIndex()) {
            val x      = xOf(i)
            val isUp   = c.close >= c.open
            g2.color   = if (isUp) Theme.CANDLE_UP else Theme.CANDLE_DN
            g2.stroke  = BasicStroke(1f)
            // Wick
            g2.draw(Line2D.Double(x, yOf(c.high), x, yOf(c.low)))
            // Body
            val top    = min(yOf(c.open), yOf(c.close))
            val bodyH  = abs(yOf(c.open) - yOf(c.close)).coerceAtLeast(1.0)
            g2.fill(RoundRectangle2D.Double(x - candleW / 2, top, candleW, bodyH, 2.0, 2.0))
        }
    }

    private fun drawLine(g2: Graphics2D, xOf: (Int) -> Double, yOf: (Double) -> Double) {
        g2.color  = Theme.ACCENT
        g2.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val path  = GeneralPath()
        candles.forEachIndexed { i, c ->
            if (i == 0) path.moveTo(xOf(i), yOf(c.close))
            else        path.lineTo(xOf(i), yOf(c.close))
        }
        g2.draw(path)
    }

    private fun drawArea(g2: Graphics2D, xOf: (Int) -> Double, yOf: (Double) -> Double, ch: Int) {
        val path = GeneralPath()
        val bottom = (pad.top + ch).toDouble()
        path.moveTo(xOf(0), bottom)
        candles.forEachIndexed { i, c -> path.lineTo(xOf(i), yOf(c.close)) }
        path.lineTo(xOf(candles.size - 1), bottom)
        path.closePath()
        val grad = GradientPaint(
            0f, pad.top.toFloat(),  Color(0, 210, 150, 120),
            0f, bottom.toFloat(),   Color(0, 210, 150, 0)
        )
        g2.paint  = grad
        g2.fill(path)
        drawLine(g2, xOf, yOf)
    }
}

// ── Stats bar ─────────────────────────────────────────────────────────────────
class StatsPanel : JPanel() {
    private val labels = mapOf(
        "OPEN"  to JLabel("–"),
        "HIGH"  to JLabel("–"),
        "LOW"   to JLabel("–"),
        "CLOSE" to JLabel("–"),
        "CHG"   to JLabel("–"),
        "BARS"  to JLabel("–")
    )

    init {
        layout = FlowLayout(FlowLayout.LEFT, 24, 6)
        background = Theme.CARD
        border = CompoundBorder(
            MatteBorder(1, 0, 0, 0, Theme.BORDER),
            EmptyBorder(4, 12, 4, 12)
        )
        labels.forEach { (title, lbl) ->
            lbl.foreground = Theme.TEXT
            lbl.font       = Font("Monospaced", Font.BOLD, 13)
            val cap = JLabel("$title ")
            cap.foreground = Theme.TEXT_DIM
            cap.font       = Font("Monospaced", Font.PLAIN, 11)
            add(cap); add(lbl)
        }
    }

    fun update(candles: List<Candle>) {
        if (candles.isEmpty()) return
        val last   = candles.last()
        val first  = candles.first()
        val chg    = last.close - first.close
        val chgPct = chg / first.close * 100
        val isUp   = chg >= 0
        fun fmt(d: Double) = "$%.2f".format(d)
        labels["OPEN"]!!.text  = fmt(last.open)
        labels["HIGH"]!!.text  = fmt(candles.maxOf { it.high })
        labels["LOW"]!!.text   = fmt(candles.minOf { it.low })
        labels["CLOSE"]!!.text = fmt(last.close)
        labels["CHG"]!!.apply {
            text       = "${if (isUp) "+" else ""}${fmt(chg)} (${"%+.2f".format(chgPct)}%)"
            foreground = if (isUp) Theme.ACCENT else Theme.RED
        }
        labels["BARS"]!!.text = "${candles.size} days"
    }
}

// ── Period selector ───────────────────────────────────────────────────────────
data class Period(val label: String, val days: Int)
val PERIODS = listOf(
    Period("1W", 7),   Period("1M", 30),  Period("3M", 90),
    Period("6M", 180), Period("1Y", 365), Period("2Y", 730),
    Period("5Y", 1825),Period("ALL", Int.MAX_VALUE)
)

// ── Main window ───────────────────────────────────────────────────────────────
class MainWindow : JFrame("Stock Market Viewer") {
    private var allCandles   = listOf<Candle>()
    private var currentDays  = 365
    private val chart        = ChartPanel(emptyList())
    private val stats        = StatsPanel()
    private val symbolField  = JTextField("IBM", 8)
    private val statusLabel  = JLabel("Enter a symbol and press Load")
    private val loadBtn      = makeButton("Load", Theme.ACCENT)
    private val periodBtns   = mutableListOf<JToggleButton>()
    private val periodGroup  = ButtonGroup()
    private val chartTypeBtns = mutableListOf<JToggleButton>()
    private val chartTypeGrp  = ButtonGroup()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        preferredSize = Dimension(1100, 680)
        contentPane.background = Theme.BG
        layout = BorderLayout(0, 0)
        buildUI()
        pack()
        setLocationRelativeTo(null)
        isVisible = true
    }

    private fun buildUI() {
        // ── Top toolbar ──
        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT, 10, 8)).apply {
            background = Theme.PANEL
            border      = MatteBorder(0, 0, 1, 0, Theme.BORDER)
        }

        val title = JLabel("📈 StockViewer")
        title.foreground = Theme.TEXT
        title.font       = Font("SansSerif", Font.BOLD, 18)
        toolbar.add(title)

        toolbar.add(Box.createHorizontalStrut(20))

        styleTextField(symbolField)
        symbolField.addActionListener { loadData() }
        toolbar.add(JLabel("Symbol:").also { it.foreground = Theme.TEXT_DIM; it.font = Font("Monospaced", Font.PLAIN, 12) })
        toolbar.add(symbolField)
        toolbar.add(loadBtn)
        loadBtn.addActionListener { loadData() }

        toolbar.add(Box.createHorizontalStrut(24))

        // Chart type toggles
        for ((type, lbl) in listOf(
            ChartPanel.ChartType.CANDLESTICK to "Candles",
            ChartPanel.ChartType.LINE        to "Line",
            ChartPanel.ChartType.AREA        to "Area"
        )) {
            val btn = makeToggleButton(lbl)
            if (type == ChartPanel.ChartType.CANDLESTICK) btn.isSelected = true
            btn.addActionListener {
                chart.chartType = type
                chart.repaint()
            }
            chartTypeGrp.add(btn)
            chartTypeBtns.add(btn)
            toolbar.add(btn)
        }

        toolbar.add(Box.createHorizontalStrut(24))
        toolbar.add(statusLabel.also { it.foreground = Theme.TEXT_DIM; it.font = Font("Monospaced", Font.PLAIN, 11) })

        add(toolbar, BorderLayout.NORTH)

        // ── Period bar ──
        val periodBar = JPanel(FlowLayout(FlowLayout.LEFT, 4, 6)).apply {
            background = Theme.PANEL
            border      = MatteBorder(0, 0, 1, 0, Theme.BORDER)
        }
        periodBar.add(JLabel("Period:").also { it.foreground = Theme.TEXT_DIM; it.font = Font("Monospaced", Font.PLAIN, 11) })

        PERIODS.forEach { period ->
            val btn = makeToggleButton(period.label)
            if (period.days == 365) btn.isSelected = true
            btn.addActionListener {
                currentDays = period.days
                applyPeriod()
            }
            periodGroup.add(btn)
            periodBtns.add(btn)
            periodBar.add(btn)
        }

        // ── Centre layout (period bar + chart) ──
        val centre = JPanel(BorderLayout()).apply { background = Theme.BG }
        centre.add(periodBar, BorderLayout.NORTH)

        val scrollChart = JScrollPane(chart).apply {
            border             = null
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy   = JScrollPane.VERTICAL_SCROLLBAR_NEVER
            background         = Theme.BG
            viewport.background = Theme.BG
        }
        centre.add(scrollChart, BorderLayout.CENTER)
        centre.add(stats, BorderLayout.SOUTH)

        add(centre, BorderLayout.CENTER)
    }

    private fun loadData() {
        val symbol = symbolField.text.trim().uppercase()
        if (symbol.isEmpty()) return
        statusLabel.text      = "Loading $symbol…"
        statusLabel.foreground = Theme.TEXT_DIM
        loadBtn.isEnabled     = false

        // Use SwingWorker properly
        object : SwingWorker<List<Candle>, Void>() {
            override fun doInBackground() = AlphaVantage.fetchDaily(symbol)
            override fun done() {
                loadBtn.isEnabled = true
                try {
                    val data = get()
                    if (data.isEmpty()) {
                        statusLabel.text      = "⚠ No data returned — check symbol or API key"
                        statusLabel.foreground = Theme.RED
                        return
                    }
                    allCandles = data
                    statusLabel.text      = "✓ $symbol  (${data.size} trading days loaded)"
                    statusLabel.foreground = Theme.ACCENT
                    applyPeriod()
                } catch (e: Exception) {
                    statusLabel.text      = "Error: ${e.message}"
                    statusLabel.foreground = Theme.RED
                }
            }
        }.execute()
    }

    private fun applyPeriod() {
        val slice = if (currentDays >= allCandles.size) allCandles
                    else allCandles.takeLast(currentDays)
        chart.setData(slice)
        stats.update(slice)
    }

    // ── Widget helpers ────────────────────────────────────────────────────────
    private fun makeButton(text: String, fg: Color) = JButton(text).apply {
        foreground       = fg
        background       = Theme.CARD
        font             = Font("Monospaced", Font.BOLD, 12)
        isFocusPainted   = false
        border           = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fg, 1, true),
            EmptyBorder(4, 14, 4, 14)
        )
        cursor           = Cursor(Cursor.HAND_CURSOR)
    }

    private fun makeToggleButton(text: String) = JToggleButton(text).apply {
        foreground     = Theme.TEXT_DIM
        background     = Theme.CARD
        font           = Font("Monospaced", Font.BOLD, 11)
        isFocusPainted = false
        border         = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            EmptyBorder(3, 10, 3, 10)
        )
        cursor         = Cursor(Cursor.HAND_CURSOR)
        addChangeListener {
            if (isSelected) {
                foreground = Theme.BG
                background = Theme.ACCENT
            } else {
                foreground = Theme.TEXT_DIM
                background = Theme.CARD
            }
        }
    }

    private fun styleTextField(tf: JTextField) {
        tf.foreground  = Theme.TEXT
        tf.background  = Theme.CARD
        tf.caretColor  = Theme.ACCENT
        tf.font        = Font("Monospaced", Font.BOLD, 13)
        tf.border      = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            EmptyBorder(4, 8, 4, 8)
        )
    }
}

// Helper import for MatteBorder
class MatteBorder(top: Int, left: Int, bottom: Int, right: Int, color: Color) :
    javax.swing.border.AbstractBorder() {
    private val insets = Insets(top, left, bottom, right)
    private val clr    = color
    override fun getBorderInsets(c: Component?) = insets
    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, w: Int, h: Int) {
        g.color = clr
        if (insets.top    > 0) g.fillRect(x, y,                            w, insets.top)
        if (insets.bottom > 0) g.fillRect(x, y + h - insets.bottom,        w, insets.bottom)
        if (insets.left   > 0) g.fillRect(x, y,                            insets.left,  h)
        if (insets.right  > 0) g.fillRect(x + w - insets.right, y,         insets.right, h)
    }
}

class CompoundBorder(private val outer: javax.swing.border.Border, private val inner: javax.swing.border.Border) :
    javax.swing.border.AbstractBorder() {
    override fun getBorderInsets(c: Component?): Insets {
        val o = outer.getBorderInsets(c)
        val i = inner.getBorderInsets(c)
        return Insets(o.top + i.top, o.left + i.left, o.bottom + i.bottom, o.right + i.right)
    }
    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, w: Int, h: Int) {
        outer.paintBorder(c, g, x, y, w, h)
        val ins = outer.getBorderInsets(c)
        inner.paintBorder(c, g, x + ins.left, y + ins.top, w - ins.left - ins.right, h - ins.top - ins.bottom)
    }
}

// ── Entry point ───────────────────────────────────────────────────────────────
fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    UIManager.put("ToggleButton.select", Theme.ACCENT)
    SwingUtilities.invokeLater { MainWindow() }
}
