package com.stockviewer.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import com.stockviewer.model.Candle
import kotlin.math.abs

@OptIn(ExperimentalTextApi::class)
@Composable
fun ChartPanel(candles: List<Candle>, chartType: ChartPanel.ChartType) {

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (candles.isEmpty()) {
            val text = "No data — enter a symbol and press Load"
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(text),
                style = TextStyle(color = Theme.TEXT_DIM, fontSize = 14.sp)
            )
            drawText(
                textLayoutResult,
                topLeft = Offset(
                    x = (size.width - textLayoutResult.size.width) / 2,
                    y = (size.height - textLayoutResult.size.height) / 2
                )
            )
            return@Canvas
        }

        val pad = Insets(top = 40f, left = 70f, bottom = 60f, right = 30f)
        val cw = size.width - pad.left - pad.right
        val ch = size.height - pad.top - pad.bottom

        val maxPrice = candles.maxOf { it.high }
        val minPrice = candles.minOf { it.low }
        val priceRange = maxPrice - minPrice

        fun xOf(idx: Int) = pad.left + idx.toFloat() / (candles.size - 1).coerceAtLeast(1) * cw
        fun yOf(p: Double) = (pad.top + ch - (p - minPrice) / priceRange * ch).toFloat()

        // Grid
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = pad.top + i.toFloat() / gridLines * ch
            drawLine(
                color = Theme.GRID,
                start = Offset(pad.left, y),
                end = Offset(pad.left + cw, y),
                strokeWidth = 1f
            )
            val price = maxPrice - i.toFloat() / gridLines * priceRange
            drawContext.canvas.nativeCanvas.apply {
                val text = "%.2f".format(price)
                drawText(
                    textMeasurer = textMeasurer,
                    text = text,
                    style = TextStyle(color = Theme.TEXT_DIM, fontSize = Theme.FONT_SIZE_SMALL),
                    topLeft = Offset(4f, y - 8)
                )
            }
        }

        // X-axis labels (show ~8 dates)
        val step = (candles.size / 8).coerceAtLeast(1)
        for (i in candles.indices step step) {
            val x = xOf(i)
            val lbl = candles[i].date.toString()
            drawContext.canvas.nativeCanvas.apply {
                val textLayoutResult = textMeasurer.measure(
                    text = AnnotatedString(lbl),
                    style = TextStyle(color = Theme.TEXT_DIM, fontSize = Theme.FONT_SIZE_SMALL)
                )
                drawText(
                    textLayoutResult,
                    topLeft = Offset(x - textLayoutResult.size.width / 2, size.height - 20)
                )
            }
        }
        fun DrawScope.drawLine(candles: List<Candle>) {
            val linePath = Path()
            candles.forEachIndexed { i, c ->
                if (i == 0) linePath.moveTo(xOf(i), yOf(c.close))
                else linePath.lineTo(xOf(i), yOf(c.close))
            }
            drawPath(linePath, color = Theme.ACCENT, style = Stroke(2f))
        }

        when (chartType) {
            ChartPanel.ChartType.LINE -> drawLine(candles)
            ChartPanel.ChartType.CANDLES -> {
                val candleW = ((cw / candles.size) * 0.6f).coerceIn(1f, 16f)
                for ((i, c) in candles.withIndex()) {
                    val x = xOf(i)
                    val isUp = c.close >= c.open
                    val color = if (isUp) Theme.CANDLE_UP else Theme.CANDLE_DN
                    // Wick
                    drawLine(
                        color = color,
                        start = Offset(x, yOf(c.high)),
                        end = Offset(x, yOf(c.low)),
                        strokeWidth = 1f
                    )
                    // Body
                    val top = yOf(c.open.coerceAtLeast(c.close))
                    val bodyH = abs(yOf(c.open) - yOf(c.close)).coerceAtLeast(1f)
                    drawRect(color, topLeft = Offset(x - candleW / 2, top), size = Size(candleW, bodyH))
                }
            }
            ChartPanel.ChartType.AREA -> {
                val path = Path()
                val bottom = pad.top + ch
                path.moveTo(xOf(0), bottom)
                candles.forEachIndexed { i, c -> path.lineTo(xOf(i), yOf(c.close)) }
                path.lineTo(xOf(candles.size - 1), bottom)
                path.close()
                drawPath(path, color = Theme.ACCENT, alpha = 0.5f)
                // Draw Line over Area
                drawLine(candles)
            }
        }

        // Border frame
        drawRect(
            color = Theme.BORDER,
            topLeft = Offset(pad.left, pad.top),
            size = Size(cw, ch),
            style = Stroke(1f)
        )
    }
}

object ChartPanel {
    enum class ChartType {
        CANDLES,
        LINE,
        AREA
    }
}

data class Insets(val top: Float, val left: Float, val bottom: Float, val right: Float)
