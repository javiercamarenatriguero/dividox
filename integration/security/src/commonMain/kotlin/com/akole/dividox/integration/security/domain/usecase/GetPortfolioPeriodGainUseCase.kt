package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.usecase.GetPriceHistoryUseCase
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

/**
 * Computes the portfolio gain (absolute and percentage) for a given [ChartPeriod], expressed in
 * the target currency.
 *
 * Each holding's gain and base value are converted from their native currency (quote currency;
 * purchase currency for the cost basis) into the target currency **before** summing, so
 * mixed-currency portfolios aggregate correctly.
 *
 * - [ChartPeriod.ALL]: gain from original cost basis — no extra API calls.
 * - [ChartPeriod.ONE_DAY]: uses [StockQuote.change] already in memory — no extra API calls.
 * - Other periods: fetches the period start price via [GetPriceHistoryUseCase] in parallel
 *   for every holding, then computes the gain from that start price to the current price.
 *
 * @return Pair of (absoluteGain, gainPercent), both 0 for an empty portfolio, or null if an
 *   exchange rate needed for the conversion is unavailable.
 */
class GetPortfolioPeriodGainUseCase(
    private val getPriceHistory: GetPriceHistoryUseCase,
    private val currencyConverter: CurrencyConverter,
) {
    suspend operator fun invoke(
        holdings: List<SecurityHolding>,
        period: ChartPeriod,
        target: Currency,
    ): Pair<Double, Double>? {
        if (holdings.isEmpty()) return 0.0 to 0.0

        val startPrices = if (period == ChartPeriod.ALL || period == ChartPeriod.ONE_DAY) {
            emptyMap()
        } else {
            fetchStartPrices(holdings, period)
        }

        var gain = 0.0
        var base = 0.0
        holdings.forEach { sh ->
            val shares = sh.holding.shares
            val quoteCurrency = sh.quoteCurrency()
            val (holdingGain, holdingBase) = when (period) {
                ChartPeriod.ALL -> {
                    val value = convert(shares * sh.quote.price, quoteCurrency, target) ?: return null
                    val cost = convert(shares * sh.holding.purchasePrice, sh.holding.purchaseCurrency, target)
                        ?: return null
                    (value - cost) to cost
                }
                ChartPeriod.ONE_DAY -> {
                    val change = convert(shares * sh.quote.change, quoteCurrency, target) ?: return null
                    val previous = convert(shares * (sh.quote.price - sh.quote.change), quoteCurrency, target)
                        ?: return null
                    change to previous
                }
                else -> {
                    val startPrice = startPrices[sh.holding.tickerId] ?: sh.quote.price
                    val change = convert(shares * (sh.quote.price - startPrice), quoteCurrency, target)
                        ?: return null
                    val start = convert(shares * startPrice, quoteCurrency, target) ?: return null
                    change to start
                }
            }
            gain += holdingGain
            base += holdingBase
        }

        val gainPct = if (base > 0.0) gain / base * 100.0 else 0.0
        return gain to gainPct
    }

    private suspend fun fetchStartPrices(
        holdings: List<SecurityHolding>,
        period: ChartPeriod,
    ): Map<String, Double> = coroutineScope {
        holdings.map { it.holding.tickerId }.distinct()
            .map { ticker ->
                async {
                    val points = runCatching { getPriceHistory(ticker, period).first() }.getOrElse { emptyList() }
                    points.firstOrNull()?.close?.let { ticker to it }
                }
            }
            .awaitAll()
            .filterNotNull()
            .toMap()
    }

    private suspend fun convert(amount: Double, from: Currency, to: Currency): Double? =
        currencyConverter.convert(amount, from, to).getOrNull()

    private fun SecurityHolding.quoteCurrency(): Currency =
        Currency.entries.firstOrNull { it.code == quote.currency } ?: Currency.USD
}
