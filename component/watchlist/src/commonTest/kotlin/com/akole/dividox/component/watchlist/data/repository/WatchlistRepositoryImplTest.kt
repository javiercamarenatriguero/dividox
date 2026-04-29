package com.akole.dividox.component.watchlist.data.repository

import com.akole.dividox.component.watchlist.data.datasource.WatchlistDataSource
import com.akole.dividox.component.watchlist.domain.model.WatchlistEntry
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WatchlistRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeDataSource = FakeWatchlistDataSource()
    private val repository = WatchlistRepositoryImpl(fakeDataSource, testDispatcher)

    @Test
    fun `SHOULD emit entries from data source WHEN getWatchlist called GIVEN populated data source`() =
        runTest(testDispatcher) {
            // GIVEN
            fakeDataSource.addEntry("AAPL")
            fakeDataSource.addEntry("MSFT")

            // WHEN
            val result = repository.getWatchlist().first()

            // THEN
            assertEquals(2, result.size)
            assertEquals("AAPL", result[0].tickerId)
            assertEquals("MSFT", result[1].tickerId)
        }

    @Test
    fun `SHOULD emit empty list WHEN getWatchlist called GIVEN empty data source`() =
        runTest(testDispatcher) {
            // GIVEN — empty data source

            // WHEN
            val result = repository.getWatchlist().first()

            // THEN
            assertTrue(result.isEmpty())
        }

    @Test
    fun `SHOULD delegate to data source WHEN addToWatchlist called`() = runTest(testDispatcher) {
        // GIVEN
        val tickerId = "TSLA"

        // WHEN
        repository.addToWatchlist(tickerId)

        // THEN
        assertTrue(fakeDataSource.addedTickers.contains(tickerId))
    }

    @Test
    fun `SHOULD delegate to data source WHEN removeFromWatchlist called`() =
        runTest(testDispatcher) {
            // GIVEN
            val tickerId = "GOOGL"

            // WHEN
            repository.removeFromWatchlist(tickerId)

            // THEN
            assertTrue(fakeDataSource.removedTickers.contains(tickerId))
        }

    @Test
    fun `SHOULD emit true WHEN isInWatchlist called for present ticker GIVEN ticker in watchlist`() =
        runTest(testDispatcher) {
            // GIVEN
            fakeDataSource.addEntry("AAPL")

            // WHEN
            val result = repository.isInWatchlist("AAPL").first()

            // THEN
            assertTrue(result)
        }

    @Test
    fun `SHOULD emit false WHEN isInWatchlist called for absent ticker GIVEN empty watchlist`() =
        runTest(testDispatcher) {
            // GIVEN — empty data source

            // WHEN
            val result = repository.isInWatchlist("AAPL").first()

            // THEN
            assertFalse(result)
        }
}

class FakeWatchlistDataSource : WatchlistDataSource {

    private val entries = MutableStateFlow<List<WatchlistEntry>>(emptyList())
    val addedTickers = mutableListOf<String>()
    val removedTickers = mutableListOf<String>()

    fun addEntry(tickerId: String) {
        entries.value = entries.value + WatchlistEntry(
            tickerId = tickerId,
            addedAt = Clock.System.now(),
        )
    }

    override fun observeWatchlist(): Flow<List<WatchlistEntry>> = entries

    override suspend fun addToWatchlist(tickerId: String) {
        addedTickers += tickerId
    }

    override suspend fun removeFromWatchlist(tickerId: String) {
        removedTickers += tickerId
    }
}
