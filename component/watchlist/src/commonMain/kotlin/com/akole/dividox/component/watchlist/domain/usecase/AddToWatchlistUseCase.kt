package com.akole.dividox.component.watchlist.domain.usecase

import com.akole.dividox.component.watchlist.domain.repository.WatchlistRepository

/**
 * Add a ticker symbol to the user's watchlist.
 * Delegates to repository which is the single source of truth.
 *
 * @param tickerId Yahoo Finance ticker symbol to add
 */
class AddToWatchlistUseCase(private val repository: WatchlistRepository) {
    suspend operator fun invoke(tickerId: String) = repository.addToWatchlist(tickerId)
}
