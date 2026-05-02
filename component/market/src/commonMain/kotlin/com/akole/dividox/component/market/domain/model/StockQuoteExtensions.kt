package com.akole.dividox.component.market.domain.model

/**
 * Returns a human-readable display label combining the ticker and the company name.
 *
 * - When [StockQuote.name] is available: `"AAPL - Apple Inc."`
 * - When no name is available: `"AAPL"`
 */
val StockQuote.displayName: String
    get() = if (name != null) "$ticker - $name" else ticker
