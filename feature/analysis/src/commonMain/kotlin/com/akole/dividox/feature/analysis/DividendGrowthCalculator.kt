package com.akole.dividox.feature.analysis

import com.akole.dividox.component.market.domain.model.MarketDividendEvent
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.feature.analysis.SecurityDetailContract.DividendGrowthBar
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal fun calculateDividendGrowthBars(
    events: List<MarketDividendEvent>,
    pricePoints: List<PricePoint>,
): List<DividendGrowthBar> {
    val dividendsByYear = events
        .groupBy { it.exDividendDate.year }
        .mapValues { (_, evts) -> evts.sumOf { it.amountPerShare } }

    val priceByYear = pricePoints
        .groupBy { it.timestamp.toLocalDateTime(TimeZone.UTC).year }
        .mapValues { (_, pts) ->
            pts.maxByOrNull { it.timestamp }?.close ?: 0.0
        }

    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.UTC).year
    return (0..9).mapNotNull { yearOffset ->
        val year = currentYear - yearOffset
        val payout = dividendsByYear[year] ?: return@mapNotNull null
        if (payout <= 0.0) return@mapNotNull null
        val price = priceByYear[year]
            ?: priceByYear.entries.filter { it.key <= year }.maxByOrNull { it.key }?.value
            ?: return@mapNotNull null
        if (price <= 0.0) return@mapNotNull null
        DividendGrowthBar(
            year = year,
            absoluteValue = payout,
            percentageOfPrice = (payout / price) * 100,
        )
    }.reversed()
}
