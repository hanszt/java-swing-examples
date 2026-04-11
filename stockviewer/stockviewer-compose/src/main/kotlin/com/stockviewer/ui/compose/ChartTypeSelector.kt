package com.stockviewer.ui.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            Button(
                onClick = { onChartTypeSelected(chartType) },
                modifier = Modifier.padding(horizontal = 4.dp),
                enabled = selectedChartType != chartType
            ) {
                Text(chartType.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}
