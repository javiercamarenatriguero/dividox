package com.akole.dividox.component.market.domain.model

import kotlin.time.Instant

data class StockQuote(
    val ticker: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val currency: String,
    val lastUpdated: Instant,
)
