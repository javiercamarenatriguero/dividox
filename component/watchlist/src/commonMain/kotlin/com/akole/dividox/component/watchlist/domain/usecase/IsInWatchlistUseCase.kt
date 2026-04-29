package com.akole.dividox.component.watchlist.domain.usecase

import com.akole.dividox.component.watchlist.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observe whether a specific ticker is currently in the user's watchlist.
 * Emits on subscription and on every watchlist change.
 *
 * @param tickerId Yahoo Finance ticker symbol to check
 * @return Flow emitting true if ticker is watched, false otherwise
 */
class IsInWatchlistUseCase(private val repository: WatchlistRepository) {
    operator fun invoke(tickerId: String): Flow<Boolean> = repository.isInWatchlist(tickerId)
}
