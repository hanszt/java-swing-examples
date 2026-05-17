package com.stockviewer.ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PeriodSelector(
    periods: List<Pair<String, Int>>,
    selectedPeriod: Int,
    onPeriodSelected: (Int) -> Unit
) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        periods.forEach { (label, days) ->
            StyledButton(
                text = label,
                onClick = { onPeriodSelected(days) },
                modifier = Modifier.padding(horizontal = 4.dp),
                selected = selectedPeriod == days
            )
        }
    }
}
