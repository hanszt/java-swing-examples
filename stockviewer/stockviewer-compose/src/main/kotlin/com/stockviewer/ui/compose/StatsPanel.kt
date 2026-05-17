package com.stockviewer.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stockviewer.model.Candle

@Composable
fun StatsPanel(candles: List<Candle>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Theme.BG)
            .border(1.dp, Theme.BORDER)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (candles.isNotEmpty()) {
            val last = candles.last()
            val first = candles.first()
            val chg = last.close - first.close
            val chgPct = chg / first.close * 100
            val isUp = chg >= 0

            fun fmt(d: Double) = "$%.2f".format(d)

            Stat("OPEN", fmt(last.open))
            Stat("HIGH", fmt(candles.maxOf { it.high }))
            Stat("LOW", fmt(candles.minOf { it.low }))
            Stat("CLOSE", fmt(last.close))
            Stat("CHG", "${if (isUp) "+" else ""}${fmt(chg)} (${"%+.2f".format(chgPct)}%)", if (isUp) Theme.CANDLE_UP else Theme.CANDLE_DN)
            Stat("BARS", "${candles.size} days")
        } else {
            Stat("OPEN", "–")
            Stat("HIGH", "–")
            Stat("LOW", "–")
            Stat("CLOSE", "–")
            Stat("CHG", "–")
            Stat("BARS", "–")
        }
    }
}

@Composable
private fun Stat(title: String, value: String, valueColor: Color = Theme.TEXT) {
    Row {
        Text(
            text = "$title ",
            color = Theme.TEXT_DIM,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
