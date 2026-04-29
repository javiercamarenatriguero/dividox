package com.akole.dividox.component.watchlist.domain.usecase

import com.akole.dividox.component.watchlist.FakeWatchlistRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoveFromWatchlistUseCaseTest {

    private val repository = FakeWatchlistRepository()
    private val useCase = RemoveFromWatchlistUseCase(repository)

    @Test
    fun `SHOULD remove ticker WHEN removeFromWatchlist is invoked GIVEN ticker in watchlist`() = runTest {
        // GIVEN
        repository.addToWatchlist("AAPL")
        repository.addToWatchlist("MSFT")

        // WHEN
        useCase("AAPL")

        // THEN
        val entries = repository.getWatchlist().first()
        assertEquals(1, entries.size)
        assertEquals("MSFT", entries[0].tickerId)
    }

    @Test
    fun `SHOULD do nothing WHEN ticker not in watchlist GIVEN remove non-existent ticker`() = runTest {
        // GIVEN
        repository.addToWatchlist("AAPL")

        // WHEN
        useCase("TSLA")

        // THEN
        val entries = repository.getWatchlist().first()
        assertEquals(1, entries.size)
        assertEquals("AAPL", entries[0].tickerId)
    }

    @Test
    fun `SHOULD produce empty list WHEN last ticker removed GIVEN single-entry watchlist`() = runTest {
        // GIVEN
        repository.addToWatchlist("GOOGL")

        // WHEN
        useCase("GOOGL")

        // THEN
        val entries = repository.getWatchlist().first()
        assertTrue(entries.isEmpty())
    }
}
