package com.stockviewer.client

import com.stockviewer.client.AlphaVantageStockDataParser.parseAlphaVantage
import com.stockviewer.client.AlphaVantageStockDataParser.parseListingStatuses
import com.stockviewer.model.Candle
import com.stockviewer.model.ListingStatus
import org.slf4j.LoggerFactory

object AlphaVantageMockDataFetcher : StockFetcher {

    private val logger = LoggerFactory.getLogger(AlphaVantageMockDataFetcher::class.java)

    override fun fetchDaily(symbol: String): List<Candle> {
        return try {
            val name = "/com/stockviewer/$symbol.json"
            AlphaVantageMockDataFetcher::class.java.getResourceAsStream(name)
                ?.bufferedReader(Charsets.UTF_8)?.readText()?.let { parseAlphaVantage(it) }
                ?: run {
                    logger.warn("No resource with symbol $symbol found at $name")
                    emptyList()
                }
        } catch (e: Exception) {
            logger.warn("Failed to fetch mock daily data for symbol: $symbol", e)
            emptyList()
        }
    }

    override fun fetchAllListingStatuses(): List<ListingStatus> {
        logger.info("Fetching all listing statuses from mock data")
        val mockCsvData = """
            IBM,International Business Machines Corp,NYSE,Stock,1962-01-02,null,Active
            IBMK,iShares iBonds Dec 2022 Term Muni Bond ETF,NYSE ARCA,ETF,2015-09-04,null,Active
            KLAC,KLA Corp,NASDAQ,Stock,1990-03-26,null,Active
            KLAG,Leverage Shares 2x Long KLAC Daily ETF,NASDAQ,Stock,2025-12-18,null,Active
            KLAR,Klarna Group plc,NYSE,Stock,2025-09-10,null,Active
            LFAW,LifeX 2060 Longevity Income ETF,BATS,ETF,2024-09-16,null,Active
            LFBD,Stone Ridge 2064 Longevity Income ETF,BATS,ETF,2025-01-06,null,Active
            LFBE,Stone Ridge 2065 Longevity Income ETF,BATS,ETF,2025-01-06,null,Active
        """.trimIndent()
        return parseListingStatuses(mockCsvData)
    }
}
