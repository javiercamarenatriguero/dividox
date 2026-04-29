package com.akole.dividox.component.watchlist.domain.usecase

import com.akole.dividox.component.watchlist.domain.repository.WatchlistRepository

/**
 * Remove a ticker symbol from the user's watchlist.
 * Delegates to repository which is the single source of truth.
 *
 * @param tickerId Yahoo Finance ticker symbol to remove
 */
class RemoveFromWatchlistUseCase(private val repository: WatchlistRepository) {
    suspend operator fun invoke(tickerId: String) = repository.removeFromWatchlist(tickerId)
}
