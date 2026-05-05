package com.akole.dividox.component.market.domain.model

import kotlinx.datetime.LocalDate

/**
 * A single historical cash dividend event as reported by the market data source.
 *
 * This represents what the company paid **per share** on the ex-dividend date.
 * The actual cash received by a user depends on their share count at that date.
 *
 * Note: [exDividendDate] is used as the event anchor, not the actual payment/settlement date,
 * because Yahoo Finance only exposes the ex-date for each dividend event.
 *
 * @property ticker Yahoo Finance ticker symbol.
 * @property amountPerShare Gross dividend per share in [currency].
 * @property exDividendDate Date on or after which buying the stock no longer qualifies for this dividend.
 * @property currency ISO 4217 currency code of the security's trading market.
 */
data class MarketDividendEvent(
    val ticker: String,
    val amountPerShare: Double,
    val exDividendDate: LocalDate,
    val currency: String,
)
