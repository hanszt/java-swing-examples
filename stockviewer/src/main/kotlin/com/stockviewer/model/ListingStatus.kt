package com.stockviewer.model

data class ListingStatus(
    val symbol: String,
    val name: String,
    val exchange: String,
    val assetType: String,
    val ipoDate: String?,
    val delistingDate: String?,
    val status: String
)
