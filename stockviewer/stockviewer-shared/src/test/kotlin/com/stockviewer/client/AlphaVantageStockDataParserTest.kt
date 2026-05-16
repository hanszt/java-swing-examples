package com.stockviewer.client

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AlphaVantageStockDataParserTest {

    @Test
    fun parseAlphaVantage() {
        val json = AlphaVantageStockDataParserTest::class.java.getResourceAsStream("/com/stockviewer/client/stock-data-sample.json")
            ?.bufferedReader()?.readText() ?: error("No resource found")
        val candles = AlphaVantageStockDataParser.parseAlphaVantage(json)

        assertEquals(candles.size, 2)
        assertEquals(LocalDate.parse("2026-03-16"), candles[0].date)

    }

}