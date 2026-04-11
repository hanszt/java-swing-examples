package com.stockviewer.viewmodel

import com.stockviewer.client.AlphaVantageFetcher
import com.stockviewer.client.AlphaVantageMockDataFetcher
import com.stockviewer.client.DataFetchMode
import com.stockviewer.client.StockFetcher
import com.stockviewer.model.Candle
import com.stockviewer.ui.compose.ChartPanel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StockViewModel(dataFetchMode: DataFetchMode, apiKey: String) {

    private val stockFetcher: StockFetcher = when (dataFetchMode) {
        DataFetchMode.MOCK_DATA -> AlphaVantageMockDataFetcher
        DataFetchMode.API_DATA -> AlphaVantageFetcher(apiKey)
    }

    private var allCandles = emptyList<Candle>()

    private val _stockData = MutableStateFlow<List<Candle>>(emptyList())
    val stockData: StateFlow<List<Candle>> = _stockData

    private val _status = MutableStateFlow("Enter a symbol and press Load")
    val status: StateFlow<String> = _status

    val periods = listOf(
        "1W" to 7,
        "1M" to 30,
        "3M" to 90,
        "6M" to 180,
        "1Y" to 365,
        "2Y" to 730,
        "5Y" to 1825,
        "ALL" to Int.MAX_VALUE
    )

    private val _selectedPeriod = MutableStateFlow(365)
    val selectedPeriod: StateFlow<Int> = _selectedPeriod

    val chartTypes = listOf(
        ChartPanel.ChartType.CANDLESTICK,
        ChartPanel.ChartType.LINE,
        ChartPanel.ChartType.AREA
    )

    private val _selectedChartType = MutableStateFlow(com.stockviewer.ui.compose.ChartPanel.ChartType.CANDLESTICK)
    val selectedChartType: StateFlow<com.stockviewer.ui.compose.ChartPanel.ChartType> = _selectedChartType

    suspend fun loadStockData(symbol: String) {
        if (symbol.isEmpty()) return
        _status.value = "Loading $symbol…"
        try {
            val data = stockFetcher.fetchDaily(symbol)
            if (data.isEmpty()) {
                _status.value = "⚠ No data returned — check symbol or API key"
            } else {
                allCandles = data
                applyPeriod()
                _status.value = "✓ $symbol  (${data.size} trading days loaded)"
            }
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }

    fun selectPeriod(days: Int) {
        _selectedPeriod.value = days
        applyPeriod()
    }

    fun selectChartType(chartType: ChartPanel.ChartType) {
        _selectedChartType.value = chartType
    }

    private fun applyPeriod() {
        val slice = if (selectedPeriod.value >= allCandles.size) allCandles
        else allCandles.takeLast(selectedPeriod.value)
        _stockData.value = slice
    }
}
