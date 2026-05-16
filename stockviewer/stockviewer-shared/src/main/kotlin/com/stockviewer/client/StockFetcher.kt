package com.stockviewer.client

import com.stockviewer.model.Candle
import com.stockviewer.model.ListingStatus

interface StockFetcher {
    suspend fun fetchDaily(symbol: String): List<Candle>
    suspend fun fetchAllListingStatuses(): List<ListingStatus>
}