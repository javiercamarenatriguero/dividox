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
    /** Security type for filtering and display. Null for plain equities. */
    val type: SecurityType? = null,
    val fiftyTwoWeekHigh: Double? = null,
    val fiftyTwoWeekLow: Double? = null,
    val volume: Long? = null,
    val dayHigh: Double? = null,
    val dayLow: Double? = null,
)
