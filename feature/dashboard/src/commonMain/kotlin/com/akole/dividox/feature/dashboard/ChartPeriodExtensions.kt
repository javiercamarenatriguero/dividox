package com.akole.dividox.feature.dashboard

import com.akole.dividox.component.market.domain.model.ChartPeriod as MarketChartPeriod
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

internal fun ChartPeriod.toStartDate(): LocalDate? {
    val today = Clock.System.todayIn(TimeZone.UTC)
    return when (this) {
        ChartPeriod.ALL -> null
        ChartPeriod.ONE_DAY -> today.minus(DatePeriod(days = 1))
        ChartPeriod.ONE_WEEK -> today.minus(DatePeriod(days = 7))
        ChartPeriod.ONE_MONTH -> today.minus(DatePeriod(months = 1))
        ChartPeriod.ONE_YEAR -> today.minus(DatePeriod(years = 1))
        ChartPeriod.YEAR_TO_DATE -> LocalDate(today.year, 1, 1)
    }
}

internal fun ChartPeriod.toMarketPeriod(): MarketChartPeriod = when (this) {
    ChartPeriod.ONE_DAY -> MarketChartPeriod.ONE_DAY
    ChartPeriod.ONE_WEEK -> MarketChartPeriod.ONE_WEEK
    ChartPeriod.ONE_MONTH -> MarketChartPeriod.ONE_MONTH
    ChartPeriod.ONE_YEAR -> MarketChartPeriod.ONE_YEAR
    ChartPeriod.YEAR_TO_DATE -> MarketChartPeriod.YTD
    ChartPeriod.ALL -> MarketChartPeriod.ALL
}
