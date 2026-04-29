package com.akole.dividox.integration.security.domain.model

import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.portfolio.domain.model.HoldingId

/**
 * Full detail view for a single security, combining market data with the user's
 * portfolio and watchlist context.
 *
 * @property ticker Yahoo Finance ticker symbol.
 * @property quote Live stock quote.
 * @property dividendInfo Dividend data; null when unavailable.
 * @property companyInfo Company metadata; null when unavailable.
 * @property priceHistory Historical price points for the requested period.
 * @property isInPortfolio True when the user holds this ticker.
 * @property isInWatchlist True when the ticker is in the user's watchlist.
 * @property holdingId Non-null only when [isInPortfolio] is true.
 */
data class SecurityDetail(
    val ticker: String,
    val quote: StockQuote,
    val dividendInfo: DividendInfo?,
    val companyInfo: CompanyInfo?,
    val priceHistory: List<PricePoint>,
    val isInPortfolio: Boolean,
    val isInWatchlist: Boolean,
    val holdingId: HoldingId?,
)
