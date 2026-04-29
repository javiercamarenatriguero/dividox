package com.akole.dividox.integration.security.domain.model

import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.watchlist.domain.model.WatchlistEntry

/**
 * A watchlist entry enriched with live market data and portfolio membership.
 *
 * @property entry Raw watchlist entry with ticker and timestamp.
 * @property quote Live stock quote; null when the market data fetch fails.
 * @property companyInfo Company metadata; null when unavailable.
 * @property isInPortfolio True when the user holds this ticker in their portfolio.
 */
data class EnrichedWatchlistEntry(
    val entry: WatchlistEntry,
    val quote: StockQuote?,
    val companyInfo: CompanyInfo?,
    val isInPortfolio: Boolean,
)
