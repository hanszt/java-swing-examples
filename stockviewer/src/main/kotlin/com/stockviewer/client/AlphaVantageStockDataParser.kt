package com.stockviewer.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.stockviewer.model.Candle
import com.stockviewer.model.ListingStatus
import org.slf4j.LoggerFactory
import java.time.LocalDate

object AlphaVantageStockDataParser {

    private val logger = LoggerFactory.getLogger(AlphaVantageStockDataParser::class.java)
    private val objectMapper = jacksonObjectMapper()

    fun parseAlphaVantage(json: String): List<Candle> {
        val node = objectMapper.readTree(json)
        val timeSeries = node.get("Time Series (Daily)")
        if (timeSeries == null) {
            logger.warn("No time series data found in response: $json")
            return emptyList()
        }

        return timeSeries.properties().reversed().map { (date, data) ->
            Candle(
                date = LocalDate.parse(date),
                open = data.get("1. open").asDouble(),
                high = data.get("2. high").asDouble(),
                low = data.get("3. low").asDouble(),
                close = data.get("4. close").asDouble(),
                volume = data.get("6. volume").asLong()
            )
        }
    }

    fun parseListingStatuses(csvData: String): List<ListingStatus> {
        return csvData.lineSequence()
            .filter { it.isNotBlank() }
            .map { line ->
                val parts = line.split(",").map { it.trim() }

                // Helper to treat the literal string "null" as a null value
                fun String.toNullable(): String? = if (this.lowercase() == "null") null else this

                ListingStatus(
                    symbol = parts[0],
                    name = parts[1],
                    exchange = parts[2],
                    assetType = parts[3],
                    ipoDate = parts[4].toNullable(),
                    delistingDate = parts[5].toNullable(),
                    status = parts[6]
                )
            }
            .toList()
    }
}