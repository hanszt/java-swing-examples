package com.stockviewer.ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stockviewer.ui.compose.ChartPanel.ChartType

@Composable
fun ChartTypeSelector(
    chartTypes: List<ChartType>,
    selectedChartType: ChartType,
    onChartTypeSelected: (ChartType) -> Unit
) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        chartTypes.forEach { chartType ->
            OutlinedButton(
                onClick = { onChartTypeSelected(chartType) },
                modifier = Modifier.padding(horizontal = 4.dp),
                border = BorderStroke(1.dp, if (selectedChartType == chartType) MaterialTheme.colors.primary else Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = if (selectedChartType == chartType) MaterialTheme.colors.primary else Color.Transparent,
                    contentColor = if (selectedChartType == chartType) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
                )
            ) {
                Text(chartType.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}
