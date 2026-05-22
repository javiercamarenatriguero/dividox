package com.akole.dividox.component.market.domain.repository

import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.MarketDividendEvent
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.NewsItem
import com.akole.dividox.component.market.domain.model.StockQuote
import kotlinx.coroutines.flow.Flow

interface MarketRepository {
    suspend fun getStockQuote(ticker: String): Result<StockQuote>
    suspend fun getMultipleQuotes(tickers: List<String>): Result<List<StockQuote>>
    suspend fun getDividendInfo(ticker: String): Result<DividendInfo>
    suspend fun getCompanyInfo(ticker: String): Result<CompanyInfo>
    suspend fun getDividendHistory(ticker: String): Result<List<DividendInfo>>

    /**
     * Returns all historical dividend events for [ticker] as individual per-share payment entries.
     *
     * Unlike [getDividendHistory] which returns aggregate [DividendInfo], this method returns
     * one entry per dividend event, enabling automatic calculation of user-specific dividend
     * amounts when multiplied by the user's share count.
     *
     * @param ticker Yahoo Finance symbol.
     * @param range Historical range. Defaults to [DividendHistoryRange.MAX] to cover any holding purchase date.
     */
    suspend fun getHistoricalDividendEvents(
        ticker: String,
        range: DividendHistoryRange = DividendHistoryRange.MAX,
    ): Result<List<MarketDividendEvent>>

    fun getPriceHistory(ticker: String, period: ChartPeriod): Flow<List<PricePoint>>
    suspend fun searchSecurities(query: String, region: String? = null): Result<List<StockQuote>>
    suspend fun getNews(query: String, count: Int = 10): Result<List<NewsItem>>
}
