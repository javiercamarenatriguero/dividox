package com.akole.dividox.integration.security.domain.model

/**
 * Aggregated financial summary of the user's portfolio.
 *
 * @property totalValue Sum of current market value across all holdings.
 * @property totalGain Absolute gain/loss (totalValue - totalCostBasis).
 * @property totalGainPercent Percentage gain/loss relative to the total cost basis.
 * @property totalYield Weighted average dividend yield across all holdings.
 * @property dividendsCollected Total annual dividend payout based on current share counts.
 * @property isDividendDataResolved false while any holding's dividend info is still pending, i.e.
 *   [totalYield] and [dividendsCollected] are not meaningful yet and should not be displayed.
 */
data class PortfolioSummary(
    val totalValue: Double,
    val totalGain: Double,
    val totalGainPercent: Double,
    val totalYield: Double,
    val dividendsCollected: Double,
    val isDividendDataResolved: Boolean = true,
)
