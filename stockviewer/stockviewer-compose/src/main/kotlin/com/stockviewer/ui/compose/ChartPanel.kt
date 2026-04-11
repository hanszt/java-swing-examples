package com.stockviewer.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.stockviewer.model.Candle
import com.stockviewer.ui.compose.Theme.ACCENT

@Composable
fun ChartPanel(candles: List<Candle>, chartType: ChartPanel.ChartType) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (candles.isEmpty()) {
            return@Canvas
        }

        val maxPrice = candles.maxOf { it.high }
        val minPrice = candles.minOf { it.low }
        val priceRange = maxPrice - minPrice
        val stepX = size.width / (candles.size - 1)

        when (chartType) {
            ChartPanel.ChartType.LINE -> {
                val path = Path()
                path.moveTo(0f, size.height - ((candles.first().close - minPrice) / priceRange * size.height).toFloat())
                candles.forEachIndexed { index, candle ->
                    val x = index * stepX
                    val y = size.height - ((candle.close - minPrice) / priceRange * size.height).toFloat()
                    path.lineTo(x, y)
                }
                drawPath(path, Color(ACCENT.rgb), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
            }
            ChartPanel.ChartType.CANDLESTICK -> {
                candles.forEachIndexed { index, candle ->
                    val x = index * stepX
                    val highY = size.height - ((candle.high - minPrice) / priceRange * size.height).toFloat()
                    val lowY = size.height - ((candle.low - minPrice) / priceRange * size.height).toFloat()
                    val openY = size.height - ((candle.open - minPrice) / priceRange * size.height).toFloat()
                    val closeY = size.height - ((candle.close - minPrice) / priceRange * size.height).toFloat()
                    val color = if (candle.open > candle.close) Color.Red else Color.Green
                    drawLine(Color.Gray, start = androidx.compose.ui.geometry.Offset(x, highY), end = androidx.compose.ui.geometry.Offset(x, lowY), strokeWidth = 2f)
                    drawRect(color, topLeft = androidx.compose.ui.geometry.Offset(x - 4, if (candle.open > candle.close) closeY else openY), size = androidx.compose.ui.geometry.Size(8f, (openY - closeY).let { if (it == 0f) 1f else kotlin.math.abs(it) }))
                }
            }
            ChartPanel.ChartType.AREA -> {
                val path = Path()
                path.moveTo(0f, size.height)
                path.lineTo(0f, size.height - ((candles.first().close - minPrice) / priceRange * size.height).toFloat())
                candles.forEachIndexed { index, candle ->
                    val x = index * stepX
                    val y = size.height - ((candle.close - minPrice) / priceRange * size.height).toFloat()
                    path.lineTo(x, y)
                }
                path.lineTo(size.width, size.height)
                path.close()
                drawPath(path, Color(Theme.ACCENT.rgb))
            }
        }
    }
}

object ChartPanel {
    enum class ChartType {
        CANDLESTICK,
        LINE,
        AREA
    }
}
