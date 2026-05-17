package com.stockviewer.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.stockviewer.client.DataFetchMode
import com.stockviewer.viewmodel.StockViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainWindow(dataFetchMode: DataFetchMode, apiKey: String, exitApplication: () -> Unit) {
    val viewModel = remember { StockViewModel(dataFetchMode, apiKey) }

    Window(onCloseRequest = exitApplication, title = "Stock Market Viewer") {
        StockMarketApp(viewModel)
    }
}

@Composable
fun StockMarketApp(viewModel: StockViewModel) {
    val stockData by viewModel.stockData.collectAsState()
    val status by viewModel.status.collectAsState()
    var symbol by remember { mutableStateOf("IBM") }
    val periods = remember { viewModel.periods }
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    StockViewerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("📈 StockViewer") },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = symbol,
                                onValueChange = { symbol = it },
                                label = { Text("Symbol") }
                            )
                            Button(onClick = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    viewModel.loadStockData(symbol)
                                }
                            }) {
                                Text("Load")
                            }
                            ChartTypeSelector(
                                chartTypes = viewModel.chartTypes,
                                selectedChartType = viewModel.selectedChartType.collectAsState().value,
                                onChartTypeSelected = { viewModel.selectChartType(it) }
                            )
                            Spacer(Modifier.width(24.dp))
                            Text(status)
                        }
                    }
                )
            },
            bottomBar = {
                StatsPanel(candles = stockData)
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                PeriodSelector(
                    periods = periods,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { viewModel.selectPeriod(it) }
                )
                if (stockData.isNotEmpty()) {
                    ChartPanel(
                        candles = stockData,
                        chartType = viewModel.selectedChartType.collectAsState().value
                    )
                }
            }
        }
    }
}
