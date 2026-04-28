package com.akole.dividox.component.market

import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.repository.MarketRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMarketRepository : MarketRepository {
    var quoteResult: Result<StockQuote> = Result.success(
        StockQuote("AAPL", 150.0, 1.5, 1.0, "USD", Clock.System.now())
    )
    var dividendInfoResult: Result<DividendInfo> = Result.success(
        DividendInfo("AAPL", 0.5, 0.96, 0.15, 5.0, null)
    )

    override suspend fun getStockQuote(ticker: String): Result<StockQuote> = quoteResult
    override suspend fun getMultipleQuotes(tickers: List<String>): Result<List<StockQuote>> =
        quoteResult.map { listOf(it) }
    override suspend fun getDividendInfo(ticker: String): Result<DividendInfo> = dividendInfoResult
    override suspend fun getCompanyInfo(ticker: String): Result<CompanyInfo> =
        Result.success(CompanyInfo("AAPL", "Apple Inc.", "NASDAQ", null))
    override suspend fun getDividendHistory(ticker: String): Result<List<DividendInfo>> =
        Result.success(emptyList())
    override fun getPriceHistory(ticker: String, period: ChartPeriod): Flow<List<PricePoint>> =
        flowOf(emptyList())
    override suspend fun searchSecurities(query: String): Result<List<StockQuote>> =
        quoteResult.map { listOf(it) }
}
