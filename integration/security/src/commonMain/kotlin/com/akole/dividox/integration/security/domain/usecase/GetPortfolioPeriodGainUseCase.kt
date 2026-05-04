package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.usecase.GetPriceHistoryUseCase
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

/**
 * Computes the portfolio gain (absolute and percentage) for a given [ChartPeriod].
 *
 * - [ChartPeriod.ALL]: gain from original cost basis — no extra API calls.
 * - [ChartPeriod.ONE_DAY]: uses [StockQuote.change] already in memory — no extra API calls.
 * - Other periods: fetches the period start price via [GetPriceHistoryUseCase] in parallel
 *   for every holding, then computes the gain from that start price to the current price.
 *
 * @return Pair of (absoluteGain, gainPercent). Both are 0 when holdings list is empty.
 */
class GetPortfolioPeriodGainUseCase(
    private val getPriceHistory: GetPriceHistoryUseCase,
) {
    suspend operator fun invoke(
        holdings: List<SecurityHolding>,
        period: ChartPeriod,
    ): Pair<Double, Double> {
        if (holdings.isEmpty()) return 0.0 to 0.0

        return when (period) {
            ChartPeriod.ALL -> computeAllTimeGain(holdings)
            ChartPeriod.ONE_DAY -> computeDayGain(holdings)
            else -> computePeriodGain(holdings, period)
        }
    }

    private fun computeAllTimeGain(holdings: List<SecurityHolding>): Pair<Double, Double> {
        val gain = holdings.sumOf { sh ->
            sh.holding.shares * (sh.quote.price - sh.holding.purchasePrice)
        }
        val costBasis = holdings.sumOf { sh -> sh.holding.shares * sh.holding.purchasePrice }
        val gainPct = if (costBasis > 0.0) gain / costBasis * 100.0 else 0.0
        return gain to gainPct
    }

    private fun computeDayGain(holdings: List<SecurityHolding>): Pair<Double, Double> {
        val gain = holdings.sumOf { sh -> sh.holding.shares * sh.quote.change }
        val prevTotal = holdings.sumOf { sh ->
            sh.holding.shares * (sh.quote.price - sh.quote.change)
        }
        val gainPct = if (prevTotal > 0.0) gain / prevTotal * 100.0 else 0.0
        return gain to gainPct
    }

    private suspend fun computePeriodGain(
        holdings: List<SecurityHolding>,
        period: ChartPeriod,
    ): Pair<Double, Double> = coroutineScope {
        // Fetch start price for every ticker in parallel.
        val startPrices: Map<String, Double> = holdings
            .map { sh ->
                async {
                    val points = runCatching { getPriceHistory(sh.holding.tickerId, period).first() }
                        .getOrElse { emptyList() }
                    sh.holding.tickerId to (points.firstOrNull()?.close ?: sh.quote.price)
                }
            }
            .map { it.await() }
            .toMap()

        val gain = holdings.sumOf { sh ->
            val startPrice = startPrices[sh.holding.tickerId] ?: sh.quote.price
            sh.holding.shares * (sh.quote.price - startPrice)
        }
        val prevTotal = holdings.sumOf { sh ->
            val startPrice = startPrices[sh.holding.tickerId] ?: sh.quote.price
            sh.holding.shares * startPrice
        }
        val gainPct = if (prevTotal > 0.0) gain / prevTotal * 100.0 else 0.0
        gain to gainPct
    }
}
