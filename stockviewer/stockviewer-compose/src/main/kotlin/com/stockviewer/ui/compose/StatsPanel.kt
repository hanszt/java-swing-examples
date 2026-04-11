package com.stockviewer.ui.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stockviewer.model.Candle

@Composable
fun StatsPanel(candles: List<Candle>) {
    if (candles.isEmpty()) {
        return
    }
    val lastCandle = candles.last()
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Open: ${lastCandle.open}", modifier = Modifier.padding(horizontal = 4.dp))
        Text("High: ${lastCandle.high}", modifier = Modifier.padding(horizontal = 4.dp))
        Text("Low: ${lastCandle.low}", modifier = Modifier.padding(horizontal = 4.dp))
        Text("Close: ${lastCandle.close}", modifier = Modifier.padding(horizontal = 4.dp))
    }
}
