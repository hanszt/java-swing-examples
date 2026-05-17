package com.stockviewer.ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StyledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false
) {
    val accent = MaterialTheme.colors.primary
    val card = MaterialTheme.colors.surface
    val textDim = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
    val onPrimary = MaterialTheme.colors.onPrimary

    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        border = BorderStroke(1.dp, if (selected) accent else card),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = if (selected) accent else card,
            contentColor = if (selected) onPrimary else textDim
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
