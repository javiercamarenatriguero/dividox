package com.akole.dividox.component.market.domain.model

import kotlin.time.Instant

data class StockQuote(
    val ticker: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val currency: String,
    val lastUpdated: Instant,
    /** Company display name, populated only from search results. Null for direct quote fetches. */
    val name: String? = null,
    /** Exchange display name (e.g. "NASDAQ", "NYSE"), populated only from search results. */
    val exchange: String? = null,
)
