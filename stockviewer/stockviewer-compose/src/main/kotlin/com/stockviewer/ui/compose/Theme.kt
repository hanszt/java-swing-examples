package com.stockviewer.ui.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Theme {
    val ACCENT = java.awt.Color(0, 210, 150)
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
