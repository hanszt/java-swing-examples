package com.stockviewer.model

import java.time.LocalDate

data class Candle(
    val date: LocalDate,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)
