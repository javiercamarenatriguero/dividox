package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.integration.security.domain.model.PortfolioSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Aggregates all enriched portfolio holdings into a single [PortfolioSummary].
 *
 * Delegates the per-holding enrichment to [GetPortfolioWithQuotesUseCase] and then
 * performs a single-pass fold over the resulting list.
 */
class GetPortfolioSummaryUseCase(
    private val getPortfolioWithQuotesUseCase: GetPortfolioWithQuotesUseCase,
) {
    operator fun invoke(): Flow<PortfolioSummary> =
        getPortfolioWithQuotesUseCase().map { securityHoldings ->
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
                val currentValue = sh.holding.shares * sh.quote.price
                val costBasis = sh.holding.shares * sh.holding.purchasePrice
                totalValue += currentValue
                totalCostBasis += costBasis

                val annualPayout = sh.dividendInfo?.annualPayout ?: 0.0
                dividendsCollected += sh.holding.shares * annualPayout

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
