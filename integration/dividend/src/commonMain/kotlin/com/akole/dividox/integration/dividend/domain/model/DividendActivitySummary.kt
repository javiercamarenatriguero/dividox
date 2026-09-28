package com.akole.dividox.integration.dividend.domain.model

/**
 * Aggregated dividend activity metrics for the dashboard summary card.
 *
 * @property lifetime All-time cumulative dividends received (base currency).
 * @property ytd Year-to-date dividends received (base currency).
 * @property yoyPercent Year-over-year percentage change in YTD dividends.
 *   `null` when no data exists for the same period last year.
 * @property nextPayout The next upcoming scheduled dividend payment, enriched
 *   with company metadata. `null` when no upcoming payments are recorded.
 */
data class DividendActivitySummary(
    val lifetime: Double,
    val ytd: Double,
    val yoyPercent: Double?,
    val nextPayout: EnrichedPayment?,
) {
    companion object {
        val Empty: DividendActivitySummary = DividendActivitySummary(
            lifetime = 0.0,
            ytd = 0.0,
            yoyPercent = null,
            nextPayout = null,
        )
    }
}
