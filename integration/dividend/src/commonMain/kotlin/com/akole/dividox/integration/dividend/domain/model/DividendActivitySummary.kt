package com.akole.dividox.integration.dividend.domain.model

import kotlinx.datetime.LocalDate

/**
 * Aggregated dividend activity metrics for the dashboard summary card.
 *
 * @property lifetime All-time cumulative dividends received (base currency).
 * @property ytd Year-to-date dividends received (base currency).
 * @property yoyPercent Year-over-year percentage change in YTD dividends.
 *   `null` when no data exists for the same period last year.
 * @property nextPayout The next upcoming scheduled dividend payment, enriched
 *   with company metadata. `null` when no upcoming payments are recorded.
 * @property yoc Yield on Cost — annualised YTD dividends divided by total
 *   portfolio cost basis, expressed as a percentage (e.g. 4.8 = 4.8%).
 * @property yocTarget Target YoC threshold. Green when [yoc] >= [yocTarget],
 *   red otherwise.
 */
data class DividendActivitySummary(
    val lifetime: Double,
    val ytd: Double,
    val yoyPercent: Double?,
    val nextPayout: EnrichedPayment?,
    val yoc: Double,
    val yocTarget: Double = YOC_TARGET_DEFAULT,
) {
    companion object {
        const val YOC_TARGET_DEFAULT: Double = 5.0

        val Empty: DividendActivitySummary = DividendActivitySummary(
            lifetime = 0.0,
            ytd = 0.0,
            yoyPercent = null,
            nextPayout = null,
            yoc = 0.0,
        )
    }
}
