package com.akole.dividox.component.watchlist

import com.akole.dividox.component.watchlist.domain.model.WatchlistEntry
import com.akole.dividox.component.watchlist.domain.repository.WatchlistRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeWatchlistRepository : WatchlistRepository {

    private val entries = MutableStateFlow<List<WatchlistEntry>>(emptyList())

    override fun getWatchlist(): Flow<List<WatchlistEntry>> = entries

    override suspend fun addToWatchlist(tickerId: String) {
        val current = entries.value
        if (current.none { it.tickerId == tickerId }) {
            entries.value = current + WatchlistEntry(
                tickerId = tickerId,
                addedAt = Clock.System.now(),
            )
        }
    }

    override suspend fun removeFromWatchlist(tickerId: String) {
        entries.value = entries.value.filterNot { it.tickerId == tickerId }
    }

    override fun isInWatchlist(tickerId: String): Flow<Boolean> =
        entries.map { list -> list.any { it.tickerId == tickerId } }
}
