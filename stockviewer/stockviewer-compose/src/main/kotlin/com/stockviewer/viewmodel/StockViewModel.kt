package com.stockviewer.viewmodel

import com.stockviewer.client.AlphaVantageFetcher
import com.stockviewer.client.AlphaVantageMockDataFetcher
import com.stockviewer.client.DataFetchMode
import com.stockviewer.client.FetchStatus
import com.stockviewer.client.StockFetcher
import com.stockviewer.model.Candle
import com.stockviewer.ui.compose.ChartPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StockViewModel(dataFetchMode: DataFetchMode, apiKey: String) {

    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val stockFetcher: StockFetcher = when (dataFetchMode) {
        DataFetchMode.MOCK_DATA -> AlphaVantageMockDataFetcher
        DataFetchMode.API_DATA -> AlphaVantageFetcher(apiKey)
    }

    private var allCandles = emptyList<Candle>()

    private val internalStockData = MutableStateFlow<List<Candle>>(emptyList())
    val stockData: StateFlow<List<Candle>> = internalStockData

    private val internalStatus = MutableStateFlow<FetchStatus>(FetchStatus.Success("Enter a symbol and press Load"))
    val status: StateFlow<FetchStatus> = internalStatus

    private val internalSymbols = MutableStateFlow<List<String>>(emptyList())
    val symbols: StateFlow<List<String>> = internalSymbols

    init {
        viewModelScope.launch {
            internalSymbols.value = stockFetcher.fetchAllListingStatuses().map { it.symbol }
        }
    }

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

    private val internalSelectedPeriod = MutableStateFlow(365)
    val selectedPeriod: StateFlow<Int> = internalSelectedPeriod

    val chartTypes = listOf(
        ChartPanel.ChartType.CANDLES,
        ChartPanel.ChartType.LINE,
        ChartPanel.ChartType.AREA
    )

    private val internalSelectedChartType = MutableStateFlow(ChartPanel.ChartType.CANDLES)
    val selectedChartType: StateFlow<ChartPanel.ChartType> = internalSelectedChartType

    suspend fun loadStockData(symbol: String) {
        if (symbol.isEmpty()) return
        internalStatus.value = FetchStatus.Success("Loading $symbol…")
        try {
            val data = stockFetcher.fetchDaily(symbol)
            if (data.isEmpty()) {
                internalStatus.value = FetchStatus.Error("⚠ No data returned for $symbol — check symbol or API key")
            } else {
                allCandles = data
                applyPeriod()
                internalStatus.value = FetchStatus.Success("✓ $symbol  (${data.size} trading days loaded)")
            }
        } catch (e: Exception) {
            internalStatus.value = FetchStatus.Error("Error: ${e.message}")
        }
    }

    fun selectPeriod(days: Int) {
        internalSelectedPeriod.value = days
        applyPeriod()
    }

    fun selectChartType(chartType: ChartPanel.ChartType) {
        internalSelectedChartType.value = chartType
    }

    private fun applyPeriod() {
        val slice = if (selectedPeriod.value >= allCandles.size) allCandles
        else allCandles.takeLast(selectedPeriod.value)
        internalStockData.value = slice
    }
}
