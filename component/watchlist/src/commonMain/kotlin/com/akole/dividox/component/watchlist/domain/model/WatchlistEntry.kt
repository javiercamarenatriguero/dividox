package com.akole.dividox.component.watchlist.domain.model

import kotlin.time.Instant

/**
 * Represents a single entry in the user's watchlist.
 *
 * @property tickerId Yahoo Finance ticker symbol (e.g. "AAPL")
 * @property addedAt Timestamp when the ticker was added to the watchlist
 */
data class WatchlistEntry(
    val tickerId: String,
    val addedAt: Instant,
)
