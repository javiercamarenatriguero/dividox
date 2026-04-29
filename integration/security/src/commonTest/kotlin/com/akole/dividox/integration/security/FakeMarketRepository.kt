package com.akole.dividox.integration.security

import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.repository.MarketRepository
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMarketRepository : MarketRepository {

    private val quotes = mutableMapOf<String, Result<StockQuote>>()
    private val dividends = mutableMapOf<String, Result<DividendInfo>>()
    private val companies = mutableMapOf<String, Result<CompanyInfo>>()
    private val priceHistories = mutableMapOf<String, List<PricePoint>>()

    fun setQuote(ticker: String, quote: StockQuote) {
        quotes[ticker] = Result.success(quote)
    }

    fun setQuoteError(ticker: String, error: Throwable) {
        quotes[ticker] = Result.failure(error)
    }

    fun setDividendInfo(ticker: String, info: DividendInfo) {
        dividends[ticker] = Result.success(info)
    }

    fun setDividendError(ticker: String, error: Throwable) {
        dividends[ticker] = Result.failure(error)
    }

    fun setCompanyInfo(ticker: String, info: CompanyInfo) {
        companies[ticker] = Result.success(info)
    }

    fun setCompanyError(ticker: String, error: Throwable) {
        companies[ticker] = Result.failure(error)
    }

    fun setPriceHistory(ticker: String, history: List<PricePoint>) {
        priceHistories[ticker] = history
    }

    override suspend fun getStockQuote(ticker: String): Result<StockQuote> =
        quotes[ticker] ?: Result.failure(IllegalStateException("No quote set for $ticker"))

    override suspend fun getMultipleQuotes(tickers: List<String>): Result<List<StockQuote>> {
        val results = tickers.mapNotNull { ticker -> quotes[ticker]?.getOrNull() }
        return Result.success(results)
    }

    override suspend fun getDividendInfo(ticker: String): Result<DividendInfo> =
        dividends[ticker] ?: Result.failure(IllegalStateException("No dividend info for $ticker"))

    override suspend fun getCompanyInfo(ticker: String): Result<CompanyInfo> =
        companies[ticker] ?: Result.failure(IllegalStateException("No company info for $ticker"))

    override suspend fun getDividendHistory(ticker: String): Result<List<DividendInfo>> =
        Result.success(emptyList())

    override fun getPriceHistory(ticker: String, period: ChartPeriod): Flow<List<PricePoint>> =
        flowOf(priceHistories[ticker] ?: emptyList())

    override suspend fun searchSecurities(query: String): Result<List<StockQuote>> =
        Result.success(emptyList())

    companion object {

        fun quote(
            ticker: String = "AAPL",
            price: Double = 150.0,
            change: Double = 1.0,
            changePercent: Double = 0.67,
            currency: String = "USD",
        ) = StockQuote(
            ticker = ticker,
            price = price,
            change = change,
            changePercent = changePercent,
            currency = currency,
            lastUpdated = Instant.fromEpochMilliseconds(0),
        )

        fun dividendInfo(
            ticker: String = "AAPL",
            yield: Double = 0.5,
            annualPayout: Double = 0.96,
            payoutRatio: Double = 0.15,
            fiveYearGrowth: Double = 0.05,
        ) = DividendInfo(
            ticker = ticker,
            yield = yield,
            annualPayout = annualPayout,
            payoutRatio = payoutRatio,
            fiveYearGrowth = fiveYearGrowth,
            exDividendDate = null,
        )

        fun companyInfo(
            ticker: String = "AAPL",
            name: String = "Apple Inc.",
            exchange: String = "NASDAQ",
        ) = CompanyInfo(
            ticker = ticker,
            name = name,
            exchange = exchange,
            logoUrl = null,
        )
    }
}
