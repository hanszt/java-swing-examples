package com.stockviewer.ui.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

object Theme {
    val ACCENT = java.awt.Color(0, 210, 150)
    val BG = Color(22, 25, 30)
    val GRID = Color(50, 55, 70)
    val BORDER = Color(80, 85, 100)
    val TEXT = Color(230, 235, 255)
    val TEXT_DIM = Color(160, 165, 180)
    val CANDLE_UP = Color(40, 200, 120)
    val CANDLE_DN = Color(240, 80, 80)
    val FONT_SIZE_SMALL = 11.sp
}

private val DarkColorPalette = darkColors(
    primary = Color(0, 210, 150),
    background = Color(10, 12, 20),
    surface = Color(16, 20, 34),
    onSurface = Color(230, 235, 255),
)

@Composable
fun StockViewerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = DarkColorPalette,
        content = content
    )
}
