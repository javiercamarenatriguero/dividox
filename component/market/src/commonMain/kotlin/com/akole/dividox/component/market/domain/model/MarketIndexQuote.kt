package com.akole.dividox.component.market.domain.model

import kotlinx.datetime.Instant

data class MarketIndexQuote(
    val name: String,
    val ticker: String,
    val marketKey: String,
    val points: Double,
    val changePoints: Double,
    val changePercent: Double,
    val lastUpdated: Instant,
)
