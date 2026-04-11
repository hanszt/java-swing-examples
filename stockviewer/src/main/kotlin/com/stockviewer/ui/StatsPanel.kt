package com.stockviewer.ui

import com.stockviewer.model.Candle
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class StatsPanel : JPanel() {
    private val labels = mapOf(
        "OPEN" to JLabel("–"),
        "HIGH" to JLabel("–"),
        "LOW" to JLabel("–"),
        "CLOSE" to JLabel("–"),
        "CHG" to JLabel("–"),
        "BARS" to JLabel("–")
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
            lbl.font = Font("Monospaced", Font.BOLD, 13)
            val cap = JLabel("$title ")
            cap.foreground = Theme.TEXT_DIM
            cap.font = Font("Monospaced", Font.PLAIN, 11)
            add(cap); add(lbl)
        }
    }

    fun update(candles: List<Candle>) {
        if (candles.isEmpty()) return
        val last = candles.last()
        val first = candles.first()
        val chg = last.close - first.close
        val chgPct = chg / first.close * 100
        val isUp = chg >= 0
        fun fmt(d: Double) = "$%.2f".format(d)
        labels.getValue("OPEN").text = fmt(last.open)
        labels.getValue("HIGH").text = fmt(candles.maxOf { it.high })
        labels.getValue("LOW").text = fmt(candles.minOf { it.low })
        labels.getValue("CLOSE").text = fmt(last.close)
        labels.getValue("CHG").apply {
            text = "${if (isUp) "+" else ""}${fmt(chg)} (${"%+.2f".format(chgPct)}%)"
            foreground = if (isUp) Theme.ACCENT else Theme.RED
        }
        labels.getValue("BARS").text = "${candles.size} days"
    }
}
