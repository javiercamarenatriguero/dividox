package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.integration.security.domain.model.PortfolioSummary
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Aggregates all enriched portfolio holdings into a single [PortfolioSummary].
 *
 * All monetary values are normalized to USD before summing, so that portfolios with
 * mixed-currency holdings (e.g. USD + EUR stocks) produce a correct total. The caller
 * is expected to convert the USD-denominated summary to the user's display currency via
 * [DashboardCurrencyExtensions.convertSummary].
 */
class GetPortfolioSummaryUseCase(
    private val getPortfolioWithQuotesUseCase: GetPortfolioWithQuotesUseCase,
    private val currencyConverter: CurrencyConverter,
) {
    operator fun invoke(): Flow<PortfolioSummary> = invoke(getPortfolioWithQuotesUseCase())

    operator fun invoke(portfolioFlow: Flow<List<SecurityHolding>>): Flow<PortfolioSummary> =
        portfolioFlow.map { securityHoldings ->
            if (securityHoldings.isEmpty()) {
                return@map PortfolioSummary(
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

                val rawCurrentValue = sh.holding.shares * sh.quote.price
                val currentValue = currencyConverter.convert(rawCurrentValue, quoteCurrency, Currency.USD)
                    .getOrElse { rawCurrentValue }

                val rawCostBasis = sh.holding.shares * sh.holding.purchasePrice
                val costBasis = currencyConverter.convert(rawCostBasis, sh.holding.purchaseCurrency, Currency.USD)
                    .getOrElse { rawCostBasis }

                totalValue += currentValue
                totalCostBasis += costBasis

                val annualPayout = sh.dividendInfo?.annualPayout ?: 0.0
                val rawDividends = sh.holding.shares * annualPayout
                dividendsCollected += currencyConverter.convert(rawDividends, quoteCurrency, Currency.USD)
                    .getOrElse { rawDividends }

                val yieldContribution = sh.dividendInfo?.yield ?: 0.0
                weightedYield += yieldContribution * currentValue
            }

            val totalGain = totalValue - totalCostBasis
            val totalGainPercent = if (totalCostBasis != 0.0) {
                (totalGain / totalCostBasis) * 100.0
            } else {
                0.0
            }
            val totalYield = if (totalValue != 0.0) weightedYield / totalValue else 0.0

            PortfolioSummary(
                totalValue = totalValue,
                totalGain = totalGain,
                totalGainPercent = totalGainPercent,
                totalYield = totalYield,
                dividendsCollected = dividendsCollected,
            )
        }
}
