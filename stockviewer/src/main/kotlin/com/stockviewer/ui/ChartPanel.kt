package com.stockviewer.ui

import com.stockviewer.model.Candle
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import java.awt.RenderingHints
import java.awt.geom.GeneralPath
import java.awt.geom.Line2D
import java.awt.geom.RoundRectangle2D
import javax.swing.JPanel
import kotlin.math.abs
import kotlin.math.min

class ChartPanel(private var candles: List<Candle>) : JPanel() {

    enum class ChartType { CANDLESTICK, LINE, AREA }

    private var chartType: ChartType = ChartType.CANDLESTICK
    private val pad = Insets(40, 70, 60, 30)

    init {
        background = Theme.BG
        preferredSize = Dimension(900, 480)
    }

    fun setData(data: List<Candle>) {
        candles = data; repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)

        val cw = width - pad.left - pad.right
        val ch = height - pad.top - pad.bottom

        if (candles.isEmpty()) {
            g2.color = Theme.TEXT_DIM
            g2.font = Font("Monospaced", Font.PLAIN, 14)
            val msg = "No data — enter a symbol and press Load"
            val fm = g2.fontMetrics
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
        g2.color = Theme.GRID
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = pad.top + i.toDouble() / gridLines * ch
            g2.drawLine(pad.left, y.toInt(), pad.left + cw, y.toInt())
            val price = maxPrice - i.toDouble() / gridLines * priceRange
            g2.color = Theme.TEXT_DIM
            g2.font = Font("Monospaced", Font.PLAIN, 11)
            g2.drawString("%.2f".format(price), 4, y.toInt() + 4)
            g2.color = Theme.GRID
        }

        // X-axis labels (show ~8 dates)
        val step = (candles.size / 8).coerceAtLeast(1)
        g2.color = Theme.TEXT_DIM
        g2.font = Font("Monospaced", Font.PLAIN, 10)
        for (i in candles.indices step step) {
            val x = xOf(i).toInt()
            val lbl = candles[i].date.toString()
            val fm = g2.fontMetrics
            g2.drawString(lbl, x - fm.stringWidth(lbl) / 2, height - 10)
        }

        when (chartType) {
            ChartType.CANDLESTICK -> drawCandlestick(g2, ::xOf, ::yOf, cw)
            ChartType.LINE -> drawLine(g2, ::xOf, ::yOf)
            ChartType.AREA -> drawArea(g2, ::xOf, ::yOf, ch)
        }

        // Border frame
        g2.color = Theme.BORDER
        g2.stroke = BasicStroke(1f)
        g2.drawRect(pad.left, pad.top, cw, ch)
    }

    private fun drawCandlestick(g2: Graphics2D, xOf: (Int) -> Double, yOf: (Double) -> Double, cw: Int) {
        val candleW = ((cw.toDouble() / candles.size) * 0.6).coerceIn(1.0, 16.0)
        for ((i, c) in candles.withIndex()) {
            val x = xOf(i)
            val isUp = c.close >= c.open
            g2.color = if (isUp) Theme.CANDLE_UP else Theme.CANDLE_DN
            g2.stroke = BasicStroke(1f)
            // Wick
            g2.draw(Line2D.Double(x, yOf(c.high), x, yOf(c.low)))
            // Body
            val top = min(yOf(c.open), yOf(c.close))
            val bodyH = abs(yOf(c.open) - yOf(c.close)).coerceAtLeast(1.0)
            g2.fill(RoundRectangle2D.Double(x - candleW / 2, top, candleW, bodyH, 2.0, 2.0))
        }
    }

    private fun drawLine(g2: Graphics2D, xOf: (Int) -> Double, yOf: (Double) -> Double) {
        g2.color = Theme.ACCENT
        g2.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val path = GeneralPath()
        candles.forEachIndexed { i, c ->
            if (i == 0) path.moveTo(xOf(i), yOf(c.close))
            else path.lineTo(xOf(i), yOf(c.close))
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
            0f, pad.top.toFloat(), Color(0, 210, 150, 120),
            0f, bottom.toFloat(), Color(0, 210, 150, 0)
        )
        g2.paint = grad
        g2.fill(path)
        drawLine(g2, xOf, yOf)
    }

    fun setType(type: ChartType) {
        chartType = type
        repaint()
    }
}
