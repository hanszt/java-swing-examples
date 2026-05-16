package com.stockviewer.client

import com.stockviewer.client.AlphaVantageStockDataParser.parseAlphaVantage
import com.stockviewer.client.AlphaVantageStockDataParser.parseListingStatuses
import com.stockviewer.model.Candle
import com.stockviewer.model.ListingStatus
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * https://www.alphavantage.co/documentation/
 */
class AlphaVantageFetcher(private val apiKey: String) : StockFetcher, AutoCloseable {

    private val logger = LoggerFactory.getLogger(AlphaVantageFetcher::class.java)

    private val domain = "https://www.alphavantage.co"
    private val client = HttpClient.newHttpClient()

    override suspend fun fetchDaily(symbol: String): List<Candle> {
        logger.info("Fetching daily data for symbol: $symbol from $domain")
        val url = "$domain/query" +
                "?function=TIME_SERIES_DAILY_ADJUSTED" +
                "&symbol=${symbol.uppercase()}" +
                "&outputsize=full" +
                "&apikey=$apiKey"
        return try {
            val req = HttpRequest.newBuilder(URI.create(url)).GET().build()
            val body = client.sendAsync(req, HttpResponse.BodyHandlers.ofString()).await().body()
            parseAlphaVantage(body)
        } catch (e: Exception) {
            logger.warn("Failed to fetch daily data for symbol: $symbol", e)
            emptyList()
        }
    }

    override suspend fun fetchAllListingStatuses(): List<ListingStatus> {
        logger.info("Fetching all listing statuses from $domain...")
        val url = "$domain/query?function=LISTING_STATUS&apikey=$apiKey"
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        return try {
            val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()

            if (response.statusCode() == 200) {
                val csvData = response.body()
                logger.info("total characters received: ${csvData.length}")
                return parseListingStatuses(csvData)
            } else {
                logger.error("Error: Received status code ${response.statusCode()}, body: ${response.body()}")
                return emptyList()
            }
        } catch (e: Exception) {
            logger.error("Error fetching listing status:", e)
            emptyList()
        }
    }

    override fun close() {
        client.close()
    }
}
