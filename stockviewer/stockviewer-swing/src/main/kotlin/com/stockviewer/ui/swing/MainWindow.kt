package com.stockviewer.ui.swing

import com.stockviewer.client.AlphaVantageFetcher
import com.stockviewer.client.AlphaVantageMockDataFetcher
import com.stockviewer.client.DataFetchMode
import com.stockviewer.model.Candle
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.plaf.basic.ComboPopup
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

class MainWindow(dataFetchMode: DataFetchMode, apiKey: String) : JFrame("Stock Market Viewer") {

    data class Period(val label: String, val days: Int)

    private val periods = listOf(
        Period("1W", 7), Period("1M", 30), Period("3M", 90),
        Period("6M", 180), Period("1Y", 365), Period("2Y", 730),
        Period("5Y", 1825), Period("ALL", Int.MAX_VALUE)
    )

    private val logger = LoggerFactory.getLogger(MainWindow::class.java)
    private val stockFetcher = when (dataFetchMode) {
        DataFetchMode.MOCK_DATA -> AlphaVantageMockDataFetcher
        DataFetchMode.API_DATA -> AlphaVantageFetcher(apiKey)
    }
    private var allCandles = emptyList<Candle>()
    private var currentDays = 365
    private val chart = ChartPanel(emptyList())
    private val stats = StatsPanel()
    private val allListingStatuses = runBlocking { stockFetcher.fetchAllListingStatuses().map { it.symbol } }
    private val symbolField = JComboBox(allListingStatuses.toTypedArray()).apply {
        isEditable = true
        selectedItem = "IBM" // Set initial value

        // Access the internal editor (which is a JTextField)
        val editor = editor.editorComponent as JTextField
        // 1. Darken the Dropdown List and ScrollPane
        val popup = getUI().getAccessibleChild(this, 0) as? ComboPopup
        val list = popup?.list
        list?.apply {
            background = Theme.BG
            foreground = Theme.TEXT
            selectionBackground = Theme.TEXT_DIM // Lighter gray for hover
        }

        // 2. Darken the Arrow Button
        // We iterate through components to find the ArrowButton
        components.forEach { comp ->
            logger.info("Found component: ${comp.javaClass.name}")
            comp.background = Theme.CARD // Dark button background
            comp.foreground = Theme.BORDER  // The actual arrow color
        }

        styleTextField(editor)

        // 1. Force Uppercase using a DocumentFilter
        (editor.document as AbstractDocument).documentFilter = object : DocumentFilter() {
            override fun insertString(fb: FilterBypass, offset: Int, string: String, attr: AttributeSet?) {
                fb.insertString(offset, string.uppercase(), attr)
            }

            override fun replace(fb: FilterBypass, offset: Int, length: Int, text: String, attr: AttributeSet?) {
                fb.replace(offset, length, text.uppercase(), attr)
            }
        }
        addActionListener { e ->
            // Update the combobox text field style on item selection
            // "comboBoxChanged" is the action command when an item is selected from the list
            if (e.actionCommand == "comboBoxChanged") {
                hidePopup()
                val selected = selectedItem as? String
                if (selected != null) {

                    // Update the text field to match the selection
                    editor.text = selected

                    // Force a text validation
                    editor.foreground = if (allListingStatuses.contains(selected)) Theme.TEXT else Theme.RED

                    // Optional: move caret to the end of the text
                    editor.caretPosition = selected.length
                }
            }
        }

        editor.document.addDocumentListener(object : DocumentListener {

            /**
             * Prevent an infinite loop when updating the text field
             */
            var isHelperWorking = false

            override fun insertUpdate(e: DocumentEvent) = filter()
            override fun removeUpdate(e: DocumentEvent) = filter()
            override fun changedUpdate(e: DocumentEvent) = filter()

            private fun filter() {
                if (isHelperWorking) return
                val text = editor.text
                if (text.isEmpty()) return

                // 1. Handle the validation
                editor.foreground = if (allListingStatuses.contains(text)) Theme.TEXT else Theme.RED

                // 2. Update the dropdown list dynamically
                SwingUtilities.invokeLater {
                    val matches = allListingStatuses.filter { it.startsWith(text, ignoreCase = true) }
                    val currentItems = (0 until model.size).map { model.getElementAt(it) }

                    if (matches != currentItems) {
                        isHelperWorking = true
                        model = DefaultComboBoxModel(matches.toTypedArray())
                        editor.text = text // Prevents model swap from clearing text
                        if (matches.isNotEmpty() && text.isNotEmpty()) {
                            showPopup()
                        } else {
                            hidePopup()
                        }
                        isHelperWorking = false
                    }
                }
            }
        })
    }
    private val statusLabel = JLabel("Enter a symbol and press Load")
    private val loadBtn = makeButton("Load", Theme.ACCENT)
    private val periodBtns = mutableListOf<JToggleButton>()
    private val periodGroup = ButtonGroup()
    private val chartTypeBtns = mutableListOf<JToggleButton>()
    private val chartTypeGrp = ButtonGroup()

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
            border = MatteBorder(0, 0, 1, 0, Theme.BORDER)
        }

        val title = JLabel("📈 StockViewer")
        title.foreground = Theme.TEXT
        title.font = Font("SansSerif", Font.BOLD, 18)
        toolbar.add(title)

        toolbar.add(Box.createHorizontalStrut(20))

        symbolField.addActionListener { loadData() }
        toolbar.add(JLabel("Symbol:").also { it.foreground = Theme.TEXT_DIM; it.font = Font("Monospaced", Font.PLAIN, 12) })
        toolbar.add(symbolField)
        toolbar.add(loadBtn)
        loadBtn.addActionListener { loadData() }

        toolbar.add(Box.createHorizontalStrut(24))

        // Chart type toggles
        for ((type, lbl) in listOf(
            ChartPanel.ChartType.CANDLESTICK to "Candles",
            ChartPanel.ChartType.LINE to "Line",
            ChartPanel.ChartType.AREA to "Area"
        )) {
            val btn = makeToggleButton(lbl)
            if (type == ChartPanel.ChartType.CANDLESTICK) btn.isSelected = true
            btn.addActionListener { chart.setType(type) }
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
            border = MatteBorder(0, 0, 1, 0, Theme.BORDER)
        }
        periodBar.add(JLabel("Period:").also { it.foreground = Theme.TEXT_DIM; it.font = Font("Monospaced", Font.PLAIN, 11) })

        periods.forEach { period ->
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
            border = null
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER
            background = Theme.BG
            viewport.background = Theme.BG
        }
        centre.add(scrollChart, BorderLayout.CENTER)
        centre.add(stats, BorderLayout.SOUTH)

        add(centre, BorderLayout.CENTER)
    }

    private fun loadData() {
        val symbol = (symbolField.selectedItem as String).trim().uppercase()
        if (symbol.isEmpty()) return
        statusLabel.text = "Loading $symbol…"
        statusLabel.foreground = Theme.TEXT_DIM
        loadBtn.isEnabled = false

        object : SwingWorker<List<Candle>, Unit>() {
            override fun doInBackground() = runBlocking { stockFetcher.fetchDaily(symbol) }

            override fun done() {
                loadBtn.isEnabled = true
                try {
                    val data = get()
                    if (data.isEmpty()) {
                        statusLabel.text = "⚠ No data returned for $symbol — check symbol or API key"
                        statusLabel.foreground = Theme.RED
                        return
                    }
                    allCandles = data
                    statusLabel.text = "✓ $symbol  (${data.size} trading days loaded)"
                    statusLabel.foreground = Theme.ACCENT
                    applyPeriod()
                } catch (e: Exception) {
                    statusLabel.text = "Error: ${e.message}"
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
        foreground = fg
        background = Theme.CARD
        font = Font("Monospaced", Font.BOLD, 12)
        isFocusPainted = false
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fg, 1, true),
            EmptyBorder(4, 14, 4, 14)
        )
        cursor = Cursor(Cursor.HAND_CURSOR)
    }

    private fun makeToggleButton(text: String) = JToggleButton(text).apply {
        foreground = Theme.TEXT_DIM
        background = Theme.CARD
        font = Font("Monospaced", Font.BOLD, 11)
        isFocusPainted = false
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            EmptyBorder(3, 10, 3, 10)
        )
        cursor = Cursor(Cursor.HAND_CURSOR)
        addChangeListener {
            if (isSelected) {
                foreground = Theme.TEXT
                background = Theme.ACCENT
            } else {
                foreground = Theme.TEXT_DIM
                background = Theme.CARD
            }
        }
    }

    private fun styleTextField(tf: JTextField) {
        tf.foreground = Theme.TEXT
        tf.background = Theme.CARD
        tf.caretColor = Theme.ACCENT
        tf.font = Font("Monospaced", Font.BOLD, 13)
        tf.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            EmptyBorder(4, 8, 4, 8)
        )
    }
}
