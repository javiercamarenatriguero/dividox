package com.akole.dividox.component.watchlist.data.repository

import com.akole.dividox.component.watchlist.data.datasource.WatchlistDataSource
import com.akole.dividox.component.watchlist.domain.model.WatchlistEntry
import com.akole.dividox.component.watchlist.domain.repository.WatchlistRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository implementation that delegates to [WatchlistDataSource].
 * Ensures all I/O operations run on [ioDispatcher].
 */
class WatchlistRepositoryImpl(
    private val dataSource: WatchlistDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : WatchlistRepository {

    override fun getWatchlist(): Flow<List<WatchlistEntry>> =
        dataSource.observeWatchlist().flowOn(ioDispatcher)

    override suspend fun addToWatchlist(tickerId: String) =
        withContext(ioDispatcher) {
            dataSource.addToWatchlist(tickerId)
        }

    override suspend fun removeFromWatchlist(tickerId: String) =
        withContext(ioDispatcher) {
            dataSource.removeFromWatchlist(tickerId)
        }

    override fun isInWatchlist(tickerId: String): Flow<Boolean> =
        dataSource.observeWatchlist()
            .map { entries -> entries.any { it.tickerId == tickerId } }
            .flowOn(ioDispatcher)
}
