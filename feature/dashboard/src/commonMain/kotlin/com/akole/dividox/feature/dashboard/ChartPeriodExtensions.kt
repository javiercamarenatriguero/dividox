package com.akole.dividox.feature.dashboard

import com.akole.dividox.component.market.domain.model.ChartPeriod as MarketChartPeriod
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.period_1d
import dividox.common.ui_resources.generated.resources.period_1m
import dividox.common.ui_resources.generated.resources.period_1w
import dividox.common.ui_resources.generated.resources.period_1y
import dividox.common.ui_resources.generated.resources.period_ytd
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.StringResource

internal fun ChartPeriod.labelRes(): StringResource = when (this) {
    ChartPeriod.ONE_DAY -> Res.string.period_1d
    ChartPeriod.ONE_WEEK -> Res.string.period_1w
    ChartPeriod.ONE_MONTH -> Res.string.period_1m
    ChartPeriod.ONE_YEAR -> Res.string.period_1y
    ChartPeriod.YEAR_TO_DATE -> Res.string.period_ytd
}

internal fun ChartPeriod.toStartDate(): LocalDate? {
    val today = Clock.System.todayIn(TimeZone.UTC)
    return when (this) {
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
}
