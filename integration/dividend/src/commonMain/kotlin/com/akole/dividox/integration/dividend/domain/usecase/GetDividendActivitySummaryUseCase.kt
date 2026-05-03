package com.akole.dividox.integration.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository
import com.akole.dividox.integration.dividend.domain.model.DividendActivitySummary
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Combines dividend, portfolio and market data into a single [DividendActivitySummary].
 *
 * The summary includes:
 * - **lifetime**: all-time cumulative dividends via [DividendRepository.getLifetimeDividends].
 * - **ytd**: year-to-date dividends via [DividendRepository.getYtdDividends].
 * - **yoyPercent**: year-over-year change computed from the full history; `null` when
 *   there are no payments in the same period last year.
 * - **nextPayout**: the earliest upcoming [EnrichedPayment]; `null` when none recorded.
 * - **yoc**: annualised YTD yield on cost = `(ytd / currentMonth * 12) / totalCostBasis * 100`.
 *
 * All monetary values are in the user's base currency as stored by the repositories.
 */
class GetDividendActivitySummaryUseCase(
    private val dividendRepository: DividendRepository,
    private val portfolioRepository: PortfolioRepository,
    private val marketRepository: MarketRepository,
) {
    operator fun invoke(): Flow<DividendActivitySummary> =
        combine(
            dividendRepository.getLifetimeDividends(),
            dividendRepository.getYtdDividends(),
            dividendRepository.getDividendHistory(),
            dividendRepository.getUpcomingPayments(),
            portfolioRepository.observePortfolio(),
        ) { lifetime, ytd, history, upcoming, holdingsResult ->

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val currentMonth = today.monthNumber

            // YoY: compare YTD amount with the same period in the previous year
            val yoyPercent = computeYoY(history, today)

            // YoC: annualise YTD dividends and divide by total cost basis
            val costBasis = holdingsResult.getOrDefault(emptyList())
                .sumOf { it.shares * it.purchasePrice }
            val yoc = if (costBasis > 0.0 && currentMonth > 0) {
                val annualisedYtd = ytd / currentMonth * 12.0
                (annualisedYtd / costBasis) * 100.0
            } else {
                0.0
            }

            // Enrich the next upcoming payment with company metadata
            val nextPayout = upcoming
                .minByOrNull { it.paymentDate }
                ?.let { payment ->
                    val info = marketRepository.getCompanyInfo(payment.tickerId).getOrNull()
                    EnrichedPayment(payment = payment, companyInfo = info)
                }

            DividendActivitySummary(
                lifetime = lifetime,
                ytd = ytd,
                yoyPercent = yoyPercent,
                nextPayout = nextPayout,
                yoc = yoc,
            )
        }

    private fun computeYoY(
        history: List<com.akole.dividox.component.dividend.domain.model.DividendPayment>,
        today: LocalDate,
    ): Double? {
        val thisYearStart = LocalDate(today.year, Month.JANUARY, 1)
        val lastYearStart = LocalDate(today.year - 1, Month.JANUARY, 1)
        val lastYearSameDayEnd = LocalDate(today.year - 1, today.month, today.dayOfMonth)

        val ytdThisYear = history
            .filter { it.paymentDate in thisYearStart..today }
            .sumOf { it.amount }

        val ytdLastYear = history
            .filter { it.paymentDate in lastYearStart..lastYearSameDayEnd }
            .sumOf { it.amount }

        return if (ytdLastYear > 0.0) {
            ((ytdThisYear - ytdLastYear) / ytdLastYear) * 100.0
        } else {
            null
        }
    }
}
