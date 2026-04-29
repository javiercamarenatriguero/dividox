package com.akole.dividox.component.watchlist.data.datasource

import com.akole.dividox.component.watchlist.domain.model.WatchlistEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data source abstraction for watchlist persistence layer.
 * Implementation accesses Firestore via dev.gitlive firebase-firestore.
 */
interface WatchlistDataSource {
    fun observeWatchlist(): Flow<List<WatchlistEntry>>
    suspend fun addToWatchlist(tickerId: String)
    suspend fun removeFromWatchlist(tickerId: String)
}
