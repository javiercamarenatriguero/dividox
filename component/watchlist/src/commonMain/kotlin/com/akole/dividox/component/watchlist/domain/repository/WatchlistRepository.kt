package com.akole.dividox.component.watchlist.domain.repository

import com.akole.dividox.component.watchlist.domain.model.WatchlistEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository for the user's watchlist. Persisted in Firestore under `users/{uid}/watchlist`.
 * Single source of truth for watchlist state—never duplicate in ViewModels.
 */
interface WatchlistRepository {

    /**
     * Observe the user's watchlist as a reactive stream.
     * Emits immediately on subscription and on every change.
     *
     * @return Flow emitting the current list of watchlist entries
     */
    fun getWatchlist(): Flow<List<WatchlistEntry>>

    /**
     * Add a ticker to the watchlist.
     * No-op if the ticker is already present.
     *
     * @param tickerId Yahoo Finance ticker symbol
     */
    suspend fun addToWatchlist(tickerId: String)

    /**
     * Remove a ticker from the watchlist.
     * No-op if the ticker is not present.
     *
     * @param tickerId Yahoo Finance ticker symbol
     */
    suspend fun removeFromWatchlist(tickerId: String)

    /**
     * Observe whether a specific ticker is in the watchlist.
     * Emits immediately and on every change.
     *
     * @param tickerId Yahoo Finance ticker symbol
     * @return Flow emitting true if ticker is in watchlist, false otherwise
     */
    fun isInWatchlist(tickerId: String): Flow<Boolean>
}
