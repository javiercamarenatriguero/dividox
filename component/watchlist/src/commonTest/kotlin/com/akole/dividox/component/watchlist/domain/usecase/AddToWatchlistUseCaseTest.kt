package com.akole.dividox.component.watchlist.domain.usecase

import com.akole.dividox.component.watchlist.FakeWatchlistRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddToWatchlistUseCaseTest {

    private val repository = FakeWatchlistRepository()
    private val useCase = AddToWatchlistUseCase(repository)

    @Test
    fun `SHOULD add ticker WHEN addToWatchlist is invoked GIVEN ticker not in watchlist`() = runTest {
        // GIVEN
        val tickerId = "AAPL"

        // WHEN
        useCase(tickerId)

        // THEN
        val entries = repository.getWatchlist().first()
        assertEquals(1, entries.size)
        assertEquals(tickerId, entries[0].tickerId)
    }

    @Test
    fun `SHOULD not duplicate WHEN same ticker added twice GIVEN ticker already in watchlist`() = runTest {
        // GIVEN
        val tickerId = "TSLA"
        useCase(tickerId)

        // WHEN
        useCase(tickerId)

        // THEN
        val entries = repository.getWatchlist().first()
        assertEquals(1, entries.size)
    }

    @Test
    fun `SHOULD add multiple tickers WHEN different tickers added GIVEN empty watchlist`() = runTest {
        // GIVEN / WHEN
        useCase("AAPL")
        useCase("GOOGL")
        useCase("MSFT")

        // THEN
        val entries = repository.getWatchlist().first()
        assertEquals(3, entries.size)
    }
}
