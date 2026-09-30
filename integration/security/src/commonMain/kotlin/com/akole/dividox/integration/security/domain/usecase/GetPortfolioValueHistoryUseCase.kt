package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.usecase.GetPriceHistoryUseCase
import com.akole.dividox.integration.security.domain.model.PortfolioValuePoint
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Builds the value evolution of the **current** portfolio over [ChartPeriod]: today's share counts
 * valued at each historical close, regardless of when each position was actually bought.
 *
 * - Series from different exchanges are aligned by calendar date; a ticker without a close on a given
 *   date carries its previous close forward (and its first close backward, for recent listings).
 * - Values are converted with today's exchange rates, so the curve reflects price moves only.
 * - The last point is today's live value, so the curve always ends at the portfolio's current total.
 * - Price series are cached per (ticker, period) for the lifetime of this instance.
 *
 * @return Chronological points, empty for an empty portfolio, or null if an exchange rate is missing.
 */
class GetPortfolioValueHistoryUseCase(
    private val getPriceHistory: GetPriceHistoryUseCase,
    private val currencyConverter: CurrencyConverter,
    private val today: () -> LocalDate = { Clock.System.now().toLocalDateTime(TimeZone.UTC).date },
) {
    private val cache = mutableMapOf<Pair<String, ChartPeriod>, List<PricePoint>>()
    private val cacheMutex = Mutex()

    suspend operator fun invoke(
        holdings: List<SecurityHolding>,
        period: ChartPeriod,
        target: Currency,
    ): List<PortfolioValuePoint>? {
        if (holdings.isEmpty()) return emptyList()

        val positions = holdings.groupBy { it.holding.tickerId }.map { (ticker, group) ->
            val quote = group.first().quote
            val rate = convert(1.0, quote.currencyOrUsd(), target) ?: return null
            Position(ticker, shares = group.sumOf { it.holding.shares }, rate = rate, livePrice = quote.price)
        }
        val series = fetchSeries(positions.map { it.ticker }, period)
        return aggregate(positions, series)
    }

    private fun aggregate(
        positions: List<Position>,
        series: Map<String, Map<LocalDate, Double>>,
    ): List<PortfolioValuePoint> {
        val today = today()
        val dates = (series.values.flatMap { it.keys } + today).distinct().sorted()
        val lastClose = mutableMapOf<String, Double>()
        return dates.map { date ->
            val value = positions.sumOf { position ->
                val closes = series[position.ticker].orEmpty()
                val close = if (date == today) {
                    position.livePrice
                } else {
                    closes[date]?.also { lastClose[position.ticker] = it }
                        ?: lastClose[position.ticker]
                        ?: closes.values.firstOrNull()
                        ?: position.livePrice
                }
                position.shares * close * position.rate
            }
            PortfolioValuePoint(date, value)
        }
    }

    private suspend fun fetchSeries(
        tickers: List<String>,
        period: ChartPeriod,
    ): Map<String, Map<LocalDate, Double>> = coroutineScope {
        tickers.map { ticker ->
            async { ticker to cachedHistory(ticker, period) }
        }.awaitAll().associate { (ticker, points) ->
            // Insertion-ordered by date so `values.firstOrNull()` is the earliest close.
            ticker to points
                .sortedBy { it.timestamp }
                .associate { it.timestamp.toLocalDateTime(TimeZone.UTC).date to it.close }
        }
    }

    private suspend fun cachedHistory(ticker: String, period: ChartPeriod): List<PricePoint> {
        val key = ticker to period
        cacheMutex.withLock { cache[key] }?.let { return it }
        val points = runCatching { getPriceHistory(ticker, period).first() }.getOrElse { emptyList() }
        if (points.isNotEmpty()) cacheMutex.withLock { cache[key] = points }
        return points
    }

    private suspend fun convert(amount: Double, from: Currency, to: Currency): Double? =
        currencyConverter.convert(amount, from, to).getOrNull()

    private fun StockQuote.currencyOrUsd(): Currency =
        Currency.entries.firstOrNull { it.code == currency } ?: Currency.USD

    private data class Position(val ticker: String, val shares: Double, val rate: Double, val livePrice: Double)
}
