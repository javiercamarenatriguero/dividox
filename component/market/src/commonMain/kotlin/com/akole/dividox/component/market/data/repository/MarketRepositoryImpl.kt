package com.akole.dividox.component.market.data.repository

import com.akole.dividox.component.market.data.api.YahooFinanceApi
import io.ktor.client.HttpClient
import com.akole.dividox.component.market.data.mapper.toCompanyInfo
import com.akole.dividox.component.market.data.mapper.toDividendInfo
import com.akole.dividox.component.market.data.mapper.toPricePoints
import com.akole.dividox.component.market.data.mapper.toStockQuote
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.MarketError
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.repository.MarketRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class MarketRepositoryImpl(
    httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
) : MarketRepository {

    private val api = YahooFinanceApi(httpClient)

    // TTL: 60s for quotes, 1h for dividend/company info
    private val quoteCache = mutableMapOf<String, Pair<StockQuote, Long>>()
    private val dividendCache = mutableMapOf<String, Pair<DividendInfo, Long>>()
    private val companyCache = mutableMapOf<String, Pair<CompanyInfo, Long>>()

    override suspend fun getStockQuote(ticker: String): Result<StockQuote> = withContext(ioDispatcher) {
        val cached = quoteCache[ticker]
        if (cached != null && !isExpired(cached.second, QUOTE_TTL_MS)) return@withContext Result.success(cached.first)
        runCatching {
            val dto = api.getChart(ticker)
            val meta = dto.chart.result?.firstOrNull()?.meta
                ?: throw MarketError.NotFound(ticker)
            meta.toStockQuote().also { quoteCache[ticker] = it to Clock.System.now().toEpochMilliseconds() }
        }.mapError()
    }

    override suspend fun getMultipleQuotes(tickers: List<String>): Result<List<StockQuote>> =
        withContext(ioDispatcher) {
            runCatching {
                coroutineScope {
                    tickers.map { ticker -> async { api.getChart(ticker) } }
                        .map { deferred ->
                            val dto = deferred.await()
                            val meta = dto.chart.result?.firstOrNull()?.meta
                            meta?.toStockQuote()
                        }
                        .filterNotNull()
                }
            }.mapError()
        }

    override suspend fun getDividendInfo(ticker: String): Result<DividendInfo> = withContext(ioDispatcher) {
        val cached = dividendCache[ticker]
        if (cached != null && !isExpired(cached.second, DIVIDEND_TTL_MS)) return@withContext Result.success(cached.first)
        runCatching {
            val dto = api.getChartWithEvents(ticker)
            val result = dto.chart.result?.firstOrNull()
                ?: throw MarketError.NotFound(ticker)
            result.toDividendInfo(ticker).also { dividendCache[ticker] = it to Clock.System.now().toEpochMilliseconds() }
        }.mapError()
    }

    override suspend fun getCompanyInfo(ticker: String): Result<CompanyInfo> = withContext(ioDispatcher) {
        val cached = companyCache[ticker]
        if (cached != null && !isExpired(cached.second, DIVIDEND_TTL_MS)) return@withContext Result.success(cached.first)
        runCatching {
            val dto = api.getChartWithEvents(ticker)
            val result = dto.chart.result?.firstOrNull()
                ?: throw MarketError.NotFound(ticker)
            result.toCompanyInfo(ticker).also { companyCache[ticker] = it to Clock.System.now().toEpochMilliseconds() }
        }.mapError()
    }

    override suspend fun getDividendHistory(ticker: String): Result<List<DividendInfo>> = withContext(ioDispatcher) {
        runCatching {
            val dto = api.getChartWithEvents(ticker)
            val result = dto.chart.result?.firstOrNull() ?: return@runCatching emptyList()
            listOf(result.toDividendInfo(ticker))
        }.mapError()
    }

    override fun getPriceHistory(ticker: String, period: ChartPeriod): Flow<List<PricePoint>> =
        flow {
            val (range, interval) = period.toRangeInterval()
            val dto = api.getChart(ticker, range = range, interval = interval)
            val result = dto.chart.result?.firstOrNull()
            emit(result?.toPricePoints() ?: emptyList())
        }.flowOn(ioDispatcher)

    override suspend fun searchSecurities(query: String): Result<List<StockQuote>> = withContext(ioDispatcher) {
        runCatching {
            val dto = api.search(query)
            dto.quotes?.filter { it.quoteType == "EQUITY" }?.map { quote ->
                StockQuote(
                    ticker = quote.symbol,
                    price = 0.0,
                    change = 0.0,
                    changePercent = 0.0,
                    currency = "",
                    lastUpdated = Clock.System.now(),
                )
            } ?: emptyList()
        }.mapError()
    }

    private fun isExpired(timestamp: Long, ttlMs: Long): Boolean =
        Clock.System.now().toEpochMilliseconds() - timestamp > ttlMs

    private fun <T> Result<T>.mapError(): Result<T> = this.recoverCatching { cause ->
        when {
            cause is MarketError -> throw cause
            cause.message?.contains("429") == true -> throw MarketError.RateLimited
            else -> throw MarketError.Unknown(cause.message ?: "Unknown error")
        }
    }

    companion object {
        private const val QUOTE_TTL_MS = 60_000L
        private const val DIVIDEND_TTL_MS = 3_600_000L
    }
}

private fun ChartPeriod.toRangeInterval(): Pair<String, String> = when (this) {
    ChartPeriod.ONE_DAY -> "1d" to "5m"
    ChartPeriod.ONE_WEEK -> "5d" to "1h"
    ChartPeriod.ONE_MONTH -> "1mo" to "1d"
    ChartPeriod.YTD -> "ytd" to "1d"
    ChartPeriod.ONE_YEAR -> "1y" to "1wk"
    ChartPeriod.ALL -> "max" to "1mo"
}
