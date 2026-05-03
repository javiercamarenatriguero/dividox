package com.akole.dividox.integration.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.integration.dividend.domain.model.MonthBar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/**
 * Builds a list of [MonthBar] entries spanning [pastMonths] of history and
 * [futureMonths] of projections for use in a dividend bar chart.
 *
 * - **Historical bars** (`isProjected = false`): derived from confirmed payment
 *   records in [DividendRepository.getDividendHistory].
 * - **Projected bars** (`isProjected = true`): derived from scheduled upcoming
 *   payments in [DividendRepository.getUpcomingPayments].
 *
 * Bars are returned sorted chronologically (oldest first).
 * Months with no payments produce a bar with `amount = 0.0`.
 *
 * @param pastMonths Number of past months to include (default 12).
 * @param futureMonths Number of future months to include (default 3).
 */
class GetDividendProjectionBarsUseCase(
    private val dividendRepository: DividendRepository,
    private val pastMonths: Int = DEFAULT_PAST_MONTHS,
    private val futureMonths: Int = DEFAULT_FUTURE_MONTHS,
) {
    operator fun invoke(): Flow<List<MonthBar>> =
        combine(
            dividendRepository.getDividendHistory(),
            dividendRepository.getUpcomingPayments(),
        ) { history, upcoming ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val thisMonthStart = LocalDate(today.year, today.month, 1)

            // Build past month keys (inclusive of current month)
            val pastKeys = (0 until pastMonths).map { offset ->
                thisMonthStart.minus(offset, DateTimeUnit.MONTH)
            }.reversed()

            // Build future month keys (excluding current month)
            val futureKeys = (1..futureMonths).map { offset ->
                thisMonthStart.monthKey(offset)
            }

            // Aggregate historical payments by month key
            val historicalTotals = history
                .groupBy { payment -> payment.paymentDate.firstOfMonth() }
                .mapValues { (_, payments) -> payments.sumOf { it.amount } }

            // Aggregate upcoming payments by month key
            val projectedTotals = upcoming
                .groupBy { payment -> payment.paymentDate.firstOfMonth() }
                .mapValues { (_, payments) -> payments.sumOf { it.amount } }

            val pastBars = pastKeys.map { key ->
                MonthBar(
                    yearMonth = key,
                    amount = historicalTotals[key] ?: 0.0,
                    isProjected = false,
                )
            }

            val futureBars = futureKeys.map { key ->
                MonthBar(
                    yearMonth = key,
                    amount = projectedTotals[key] ?: 0.0,
                    isProjected = true,
                )
            }

            pastBars + futureBars
        }

    private fun LocalDate.firstOfMonth(): LocalDate = LocalDate(year, month, 1)

    private fun LocalDate.monthKey(offsetMonths: Int): LocalDate {
        var y = year
        var m = monthNumber + offsetMonths
        while (m > 12) {
            m -= 12
            y++
        }
        return LocalDate(y, Month(m), 1)
    }

    companion object {
        const val DEFAULT_PAST_MONTHS: Int = 12
        const val DEFAULT_FUTURE_MONTHS: Int = 3
    }
}
