package com.akole.dividox.feature.analysis

import com.akole.dividox.common.ui.resources.charts.LineChartEntry
import com.akole.dividox.common.ui.resources.charts.calculateChartFloor
import com.akole.dividox.common.ui.resources.format.monthShort
import com.akole.dividox.common.ui.resources.format.monthShortWithYear
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.PricePoint
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal data class PriceChartData(
    val entries: List<LineChartEntry>,
    val floorPrice: Float,
)

internal fun List<PricePoint>.toPriceChartData(period: ChartPeriod): PriceChartData {
    val thinned = thinForPeriod(period)
    val minPrice = thinned.minOfOrNull { it.close.toFloat() } ?: 0f
    val maxPrice = thinned.maxOfOrNull { it.close.toFloat() } ?: minPrice
    val floorPrice = calculateChartFloor(minPrice, maxPrice)
    val entries = thinned.mapIndexed { index, point ->
        val reverseIndex = thinned.size - 1 - index
        val dt = point.timestamp.toLocalDateTime(TimeZone.UTC)
        val sparseLabel = period == ChartPeriod.ALL || period == ChartPeriod.FIVE_YEARS
        val label = when {
            period == ChartPeriod.ONE_DAY && dt.minute != 0 ->
                "​".repeat(index + 1)
            period == ChartPeriod.ONE_MONTH && dt.day % 5 != 0 ->
                "​".repeat(index + 1)
            sparseLabel && reverseIndex % 2 != 0 ->
                "​".repeat(index + 1)
            else -> point.toChartLabel(period)
        }
        LineChartEntry(
            label = label,
            value = point.close.toFloat() - floorPrice,
            tooltipLabel = point.toChartLabel(period),
        )
    }
    return PriceChartData(entries = entries, floorPrice = floorPrice)
}

private fun PricePoint.toChartLabel(period: ChartPeriod): String {
    val dt = timestamp.toLocalDateTime(TimeZone.UTC)
    return when (period) {
        ChartPeriod.ONE_DAY -> "${dt.hour}:${dt.minute.toString().padStart(2, '0')}"
        ChartPeriod.ONE_WEEK, ChartPeriod.ONE_MONTH -> "${dt.day} ${dt.date.monthShort()}"
        ChartPeriod.YTD, ChartPeriod.ONE_YEAR -> dt.date.monthShort()
        ChartPeriod.FIVE_YEARS -> dt.date.monthShortWithYear()
        ChartPeriod.ALL -> dt.year.toString()
    }
}

private fun List<PricePoint>.thinForPeriod(period: ChartPeriod): List<PricePoint> {
    return when (period) {
        // ONE_YEAR uses weekly data spanning 2 calendar years (e.g. Jun 2024 – May 2025).
        // Grouping by month produces 13 groups where the boundary month name appears twice
        // (e.g. "May" for both 2024 and 2025). Taking the last 12 guarantees 12 consecutive
        // months with no duplicate short-name labels, so the library's chartData map size
        // matches entries.size and the scrubber overlay stays aligned.
        ChartPeriod.ONE_YEAR -> groupBy {
            val dt = it.timestamp.toLocalDateTime(TimeZone.UTC)
            "${dt.year}-${(dt.month.ordinal + 1).toString().padStart(2, '0')}"
        }.entries.sortedBy { it.key }.takeLast(12).mapNotNull { it.value.lastOrNull() }

        // YTD spans only the current calendar year — months are always unique within a year.
        ChartPeriod.YTD -> groupBy {
            val dt = it.timestamp.toLocalDateTime(TimeZone.UTC)
            "${dt.year}-${(dt.month.ordinal + 1).toString().padStart(2, '0')}"
        }.entries.sortedBy { it.key }.mapNotNull { it.value.lastOrNull() }

        // ONE_WEEK uses hourly data with daily labels — deduplicate to one point per day.
        ChartPeriod.ONE_WEEK -> groupBy {
            val dt = it.timestamp.toLocalDateTime(TimeZone.UTC)
            "${dt.year}-${(dt.month.ordinal + 1).toString().padStart(2, '0')}-${dt.day.toString().padStart(2, '0')}"
        }.entries.sortedBy { it.key }.mapNotNull { it.value.lastOrNull() }

        ChartPeriod.FIVE_YEARS -> groupBy {
            val dt = it.timestamp.toLocalDateTime(TimeZone.UTC)
            "${dt.year}-Q${dt.month.ordinal / 3}"
        }.entries.sortedBy { it.key }.mapNotNull { it.value.lastOrNull() }

        ChartPeriod.ALL -> {
            val maxYear = maxOfOrNull { it.timestamp.toLocalDateTime(TimeZone.UTC).year }
                ?: return this
            filter { it.timestamp.toLocalDateTime(TimeZone.UTC).year >= maxYear - 14 }
                .groupBy { it.timestamp.toLocalDateTime(TimeZone.UTC).year }
                .entries.sortedBy { it.key }
                .mapNotNull { it.value.lastOrNull() }
        }

        else -> this
    }
}
