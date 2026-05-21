package com.akole.dividox.component.market.domain.model

internal data class MarketIndexEntry(
    val displayName: String,
    val ticker: String,
    val marketKey: String,
)

internal val MARKET_INDICES = listOf(
    MarketIndexEntry("S&P 500", "^GSPC", "US"),
    MarketIndexEntry("Nasdaq Composite", "^IXIC", "US"),
    MarketIndexEntry("Euro Stoxx 50", "^STOXX50E", "EU"),
    MarketIndexEntry("IBEX 35", "^IBEX", "ES"),
    MarketIndexEntry("DAX", "^GDAXI", "DE"),
    MarketIndexEntry("Nikkei 225", "^N225", "JP"),
    MarketIndexEntry("FTSE 100", "^FTSE", "UK"),
)
