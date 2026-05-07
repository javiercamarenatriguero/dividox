package com.akole.dividox.feature.dividends

import com.akole.dividox.common.ui.resources.di.todayIn
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import com.akole.dividox.integration.dividend.domain.model.MonthBar
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

internal fun List<EnrichedPayment>.groupByMonth(): Map<LocalDate, List<EnrichedPayment>> =
    groupBy { LocalDate(it.payment.paymentDate.year, it.payment.paymentDate.month, 1) }
        .entries
        .sortedByDescending { it.key }
        .associate { it.key to it.value }

internal fun List<MonthBar>.filterByRange(range: DividendHistoryRange): List<MonthBar> {
    val today = todayIn(TimeZone.currentSystemDefault())
    val cutoff: LocalDate = when (range) {
        DividendHistoryRange.YTD -> LocalDate(today.year, Month.JANUARY, 1)
        DividendHistoryRange.ONE_YEAR -> today.minus(12, DateTimeUnit.MONTH)
        DividendHistoryRange.FIVE_YEARS -> today.minus(60, DateTimeUnit.MONTH)
        DividendHistoryRange.MAX -> LocalDate(1970, Month.JANUARY, 1)
    }
    val filtered = filter { it.yearMonth >= cutoff }
    return when (range) {
        DividendHistoryRange.FIVE_YEARS, DividendHistoryRange.MAX -> filtered.aggregateByYear()
        else -> filtered
    }
}

internal fun List<MonthBar>.aggregateByYear(): List<MonthBar> =
    groupBy { it.yearMonth.year }
        .entries
        .sortedBy { it.key }
        .map { (year, bars) ->
            MonthBar(
                yearMonth = LocalDate(year, Month.JANUARY, 1),
                amount = bars.sumOf { it.amount },
                isProjected = bars.any { it.isProjected },
            )
        }
