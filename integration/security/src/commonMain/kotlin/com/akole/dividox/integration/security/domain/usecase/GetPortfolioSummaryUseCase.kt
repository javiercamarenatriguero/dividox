package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.integration.security.domain.model.PortfolioSummary
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

/**
 * Aggregates all enriched portfolio holdings into a single [PortfolioSummary].
 *
 * Every monetary value is converted **directly** from its native currency (quote currency for
 * market value/dividends, purchase currency for cost basis) into the target currency. There is no
 * intermediate USD pivot, which previously introduced cross-rate rounding (e.g. EUR→USD→EUR ≠ 1).
 *
 * If any required exchange rate is unavailable, [summarize] returns null instead of silently adding
 * amounts expressed in different currencies — callers keep showing the last correct value.
 */
class GetPortfolioSummaryUseCase(
    private val getPortfolioWithQuotesUseCase: GetPortfolioWithQuotesUseCase,
    private val currencyConverter: CurrencyConverter,
) {
    /** USD-denominated summary stream. */
    operator fun invoke(): Flow<PortfolioSummary> = invoke(getPortfolioWithQuotesUseCase())

    /** USD-denominated summary stream for [portfolioFlow]. */
    operator fun invoke(portfolioFlow: Flow<List<SecurityHolding>>): Flow<PortfolioSummary> =
        portfolioFlow.mapNotNull { summarize(it, Currency.USD) }

    /**
     * Computes the summary of [securityHoldings] expressed in [target] currency.
     * @return null if any exchange rate needed for the conversion is unavailable.
     */
    @Suppress("ReturnCount")
    suspend fun summarize(securityHoldings: List<SecurityHolding>, target: Currency): PortfolioSummary? {
        if (securityHoldings.isEmpty()) {
            return PortfolioSummary(
                totalValue = 0.0,
                totalGain = 0.0,
                totalGainPercent = 0.0,
                totalYield = 0.0,
                dividendsCollected = 0.0,
            )
        }

        var totalValue = 0.0
        var totalCostBasis = 0.0
        var weightedYield = 0.0
        var dividendsCollected = 0.0

        securityHoldings.forEach { sh ->
            val quoteCurrency = Currency.entries.firstOrNull { it.code == sh.quote.currency } ?: Currency.USD

            val currentValue = currencyConverter
                .convert(sh.holding.shares * sh.quote.price, quoteCurrency, target)
                .getOrNull() ?: return null

            val costBasis = currencyConverter
                .convert(sh.holding.shares * sh.holding.purchasePrice, sh.holding.purchaseCurrency, target)
                .getOrNull() ?: return null

            totalValue += currentValue
            totalCostBasis += costBasis

            val annualPayout = sh.dividendInfo?.annualPayout ?: 0.0
            if (annualPayout != 0.0) {
                dividendsCollected += currencyConverter
                    .convert(sh.holding.shares * annualPayout, quoteCurrency, target)
                    .getOrNull() ?: return null
            }

            weightedYield += (sh.dividendInfo?.yield ?: 0.0) * currentValue
        }

        val totalGain = totalValue - totalCostBasis
        val totalGainPercent = if (totalCostBasis != 0.0) (totalGain / totalCostBasis) * 100.0 else 0.0
        val totalYield = if (totalValue != 0.0) weightedYield / totalValue else 0.0

        return PortfolioSummary(
            totalValue = totalValue,
            totalGain = totalGain,
            totalGainPercent = totalGainPercent,
            totalYield = totalYield,
            dividendsCollected = dividendsCollected,
            isDividendDataResolved = securityHoldings.all { it.isDividendInfoResolved },
        )
    }
}
