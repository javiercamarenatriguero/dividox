package com.akole.dividox.component.market

import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.component.market.domain.model.MarketDividendEvent
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.NewsItem
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.repository.MarketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMarketRepository : MarketRepository {
    var quoteResult: Result<StockQuote> = Result.success(
        StockQuote("AAPL", 150.0, 1.5, 1.0, "USD", kotlin.time.Clock.System.now())
    )
    var multipleQuotesResult: Result<List<StockQuote>>? = null
    var dividendInfoResult: Result<DividendInfo> = Result.success(
        DividendInfo("AAPL", 0.5, 0.96, 0.15, 5.0, null)
    )
    var companyInfoResult: Result<CompanyInfo> = Result.success(
        CompanyInfo("AAPL", "Apple Inc.", "NASDAQ", null)
    )
    var dividendHistoryResult: Result<List<DividendInfo>> = Result.success(emptyList())
    var priceHistoryResult: List<PricePoint> = emptyList()

    override suspend fun getStockQuote(ticker: String): Result<StockQuote> = quoteResult
    override suspend fun getMultipleQuotes(tickers: List<String>): Result<List<StockQuote>> =
        multipleQuotesResult ?: quoteResult.map { listOf(it) }
    override suspend fun getDividendInfo(ticker: String): Result<DividendInfo> = dividendInfoResult
    override suspend fun getCompanyInfo(ticker: String): Result<CompanyInfo> = companyInfoResult
    override suspend fun getDividendHistory(ticker: String): Result<List<DividendInfo>> = dividendHistoryResult
    override suspend fun getHistoricalDividendEvents(
        ticker: String,
        range: DividendHistoryRange,
    ): Result<List<MarketDividendEvent>> = Result.success(emptyList())

    override fun getPriceHistory(ticker: String, period: ChartPeriod): Flow<List<PricePoint>> =
        flowOf(priceHistoryResult)
    override suspend fun searchSecurities(query: String, region: String?): Result<List<StockQuote>> =
        quoteResult.map { listOf(it) }

    var newsResult: Result<List<NewsItem>> = Result.success(emptyList())
    override suspend fun getNews(query: String, count: Int): Result<List<NewsItem>> = newsResult
}
