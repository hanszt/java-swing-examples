package com.stockviewer.ui.compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.stockviewer.client.DataFetchMode
import com.stockviewer.client.FetchStatus
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
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Symbol:")
                            val symbols by viewModel.symbols.collectAsState()
                            AutoCompleteTextField(
                                value = symbol,
                                onValueChange = { symbol = it },
                                suggestions = symbols,
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = MaterialTheme.colors.onSurface,
                                    backgroundColor = MaterialTheme.colors.surface,
                                    cursorColor = MaterialTheme.colors.primary
                                )
                            )
                            LoadButton(
                                text = "Load",
                                onClick = {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        viewModel.loadStockData(symbol)
                                    }
                                }
                            )
                            ChartTypeSelector(
                                chartTypes = viewModel.chartTypes,
                                selectedChartType = viewModel.selectedChartType.collectAsState().value,
                                onChartTypeSelected = { viewModel.selectChartType(it) }
                            )
                            Spacer(Modifier.width(24.dp))
                            when (val s = status) {
                                is FetchStatus.Success<*> -> Text(s.data.toString(), color = MaterialTheme.colors.onSurface)
                                is FetchStatus.Error -> Text(s.message, color = MaterialTheme.colors.error)
                            }
                        }
                    }
                )
            },
            bottomBar = {
                StatsPanel(candles = stockData)
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colors.surface,
                    elevation = 4.dp
                ) {
                    PeriodSelector(
                        periods = periods,
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { viewModel.selectPeriod(it) }
                    )
                }
                ChartPanel(
                    candles = stockData,
                    chartType = viewModel.selectedChartType.collectAsState().value
                )
            }
        }
    }
}
