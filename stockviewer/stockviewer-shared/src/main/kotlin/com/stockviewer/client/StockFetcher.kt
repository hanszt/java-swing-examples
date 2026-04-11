package com.stockviewer.client

import com.stockviewer.model.Candle
import com.stockviewer.model.ListingStatus

interface StockFetcher {
    fun fetchDaily(symbol: String): List<Candle>
    fun fetchAllListingStatuses(): List<ListingStatus>
}