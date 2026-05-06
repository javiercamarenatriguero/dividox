package com.akole.dividox.component.market.data.repository

import com.akole.dividox.component.market.data.api.YahooFinanceApi
import io.ktor.client.HttpClient
import com.akole.dividox.component.market.data.mapper.toCompanyInfo
import com.akole.dividox.component.market.data.mapper.toDividendInfo
import com.akole.dividox.component.market.data.mapper.toMarketDividendEvents
import com.akole.dividox.component.market.data.mapper.toPricePoints
import com.akole.dividox.component.market.data.mapper.toStockQuote
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.MarketDividendEvent
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

    private val quoteCache = mutableMapOf<String, Pair<StockQuote, Long>>()
    private val dividendCache = mutableMapOf<String, Pair<DividendInfo, Long>>()
    private val companyCache = mutableMapOf<String, Pair<CompanyInfo, Long>>()
    private val historicalDividendCache = mutableMapOf<String, Pair<List<MarketDividendEvent>, Long>>()

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
                    val now = Clock.System.now().toEpochMilliseconds()
                    val fromCache = mutableListOf<StockQuote>()
                    val toFetch = mutableListOf<String>()
                    tickers.forEach { ticker ->
                        val cached = quoteCache[ticker]
                        if (cached != null && !isExpired(cached.second, QUOTE_TTL_MS)) {
                            fromCache += cached.first
                        } else {
                            toFetch += ticker
                        }
                    }
                    val fromApi = toFetch
                        .map { ticker -> async { api.getChart(ticker) to ticker } }
                        .map { deferred ->
                            val (dto, ticker) = deferred.await()
                            dto.chart.result?.firstOrNull()?.meta
                                ?.toStockQuote()
                                ?.also { quote -> quoteCache[ticker] = quote to now }
                        }
                        .filterNotNull()
                    fromCache + fromApi
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

    override suspend fun getHistoricalDividendEvents(
        ticker: String,
        range: DividendHistoryRange,
    ): Result<List<MarketDividendEvent>> = withContext(ioDispatcher) {
        val cacheKey = "$ticker:${range.apiValue}"
        val cached = historicalDividendCache[cacheKey]
        if (cached != null && !isExpired(cached.second, HISTORICAL_DIVIDEND_TTL_MS)) {
            return@withContext Result.success(cached.first)
        }
        runCatching {
            val dto = api.getChartWithEvents(ticker, range = range.apiValue)
            val result = dto.chart.result?.firstOrNull() ?: return@runCatching emptyList()
            result.toMarketDividendEvents(ticker).also { events ->
                historicalDividendCache[cacheKey] = events to Clock.System.now().toEpochMilliseconds()
            }
        }.mapError()
    }

    override fun getPriceHistory(ticker: String, period: ChartPeriod): Flow<List<PricePoint>> =
        flow {
            val (range, interval) = period.toRangeInterval()
            val dto = api.getChart(ticker, range = range, interval = interval)
            val result = dto.chart.result?.firstOrNull()
            emit(result?.toPricePoints() ?: emptyList())
        }.flowOn(ioDispatcher)

    override suspend fun searchSecurities(query: String, region: String?): Result<List<StockQuote>> = withContext(ioDispatcher) {
        runCatching {
            val dto = api.search(query, region)
            val acceptedTypes = setOf("EQUITY")
            dto.quotes
                ?.filter { it.quoteType?.uppercase() in acceptedTypes }
                ?.map { quote ->
                    StockQuote(
                        ticker = quote.symbol,
                        price = 0.0,
                        change = 0.0,
                        changePercent = 0.0,
                        currency = "",
                        lastUpdated = Clock.System.now(),
                        name = quote.shortname ?: quote.longname,
                        exchange = quote.exchDisp,
                    )
                }
                ?.sortedBy { exchangePriority(it.exchange) }
                ?: emptyList()
        }.mapError()
    }

    private fun exchangePriority(exchange: String?): Int {
        val e = exchange?.uppercase() ?: return Int.MAX_VALUE
        return EXCHANGE_PRIORITY.indexOfFirst { e.contains(it) }
            .takeIf { it >= 0 } ?: Int.MAX_VALUE
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
        private const val QUOTE_TTL_MS = 300_000L              // 5 minutes
        private const val DIVIDEND_TTL_MS = 3_600_000L         // 1 hour
        private const val HISTORICAL_DIVIDEND_TTL_MS = 86_400_000L  // 24 hours

        /**
         * Main exchange tiers, ordered from highest to lowest priority.
         * Yahoo Finance returns exchDisp values like "NASDAQ", "NYSE", "LSE", etc.
         * Unlisted or exotic exchanges fall to Int.MAX_VALUE and stay at the bottom.
         */
        private val EXCHANGE_PRIORITY = listOf(
            "NASDAQ", "NYSE",            // US — tier 1
            "LSE",                       // UK
            "XETRA", "FSX", "FRA",      // Germany
            "EURONEXT", "PAR", "AMS",   // Euronext
            "BME", "MCE",               // Spain
            "MIL",                       // Italy
            "SIX", "SWX",               // Switzerland
            "TSX",                       // Canada
            "ASX",                       // Australia
            "TSE", "OSE",               // Japan
            "HKEX", "HKG",              // Hong Kong
        )
    }
}

private fun ChartPeriod.toRangeInterval(): Pair<String, String> = when (this) {
    ChartPeriod.ONE_DAY -> "1d" to "5m"
    ChartPeriod.ONE_WEEK -> "5d" to "1h"
    ChartPeriod.ONE_MONTH -> "1mo" to "1d"
    ChartPeriod.YTD -> "ytd" to "1d"
    ChartPeriod.ONE_YEAR -> "1y" to "1wk"
    ChartPeriod.FIVE_YEARS -> "5y" to "1mo"
    ChartPeriod.ALL -> "max" to "1mo"
}
