package com.akole.dividox.component.watchlist.domain.usecase

import com.akole.dividox.component.watchlist.domain.model.WatchlistEntry
import com.akole.dividox.component.watchlist.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observe the user's watchlist as a reactive stream.
 * Emits every time the watchlist changes.
 */
class GetWatchlistUseCase(private val repository: WatchlistRepository) {
    operator fun invoke(): Flow<List<WatchlistEntry>> = repository.getWatchlist()
}
