package com.akole.dividox.integration.security.domain.model

import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.portfolio.domain.model.Holding

/**
 * A portfolio holding enriched with live market data.
 *
 * @property holding Raw holding with purchase details.
 * @property quote Live stock quote for the holding's ticker.
 * @property dividendInfo Dividend data for the ticker; null when unavailable.
 * @property totalGainPercent Percentage gain/loss relative to the cost basis:
 *   `(currentValue - costBasis) / costBasis * 100`.
 * @property isDividendInfoResolved false while [dividendInfo] has not been fetched yet
 *   (null means "pending"); true once fetched, even if the result is null (unavailable).
 */
data class SecurityHolding(
    val holding: Holding,
    val quote: StockQuote,
    val dividendInfo: DividendInfo?,
    val totalGainPercent: Double,
    val isDividendInfoResolved: Boolean = true,
)
