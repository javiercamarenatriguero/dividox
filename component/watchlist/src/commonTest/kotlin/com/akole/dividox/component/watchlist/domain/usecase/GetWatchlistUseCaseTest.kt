package com.akole.dividox.component.watchlist.domain.usecase

import com.akole.dividox.component.watchlist.FakeWatchlistRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetWatchlistUseCaseTest {

    private val repository = FakeWatchlistRepository()
    private val useCase = GetWatchlistUseCase(repository)

    @Test
    fun `SHOULD emit empty list WHEN watchlist is empty GIVEN initial state`() = runTest {
        // GIVEN — fresh repository

        // WHEN
        val result = useCase().first()

        // THEN
        assertTrue(result.isEmpty())
    }

    @Test
    fun `SHOULD emit entries WHEN items are present GIVEN populated watchlist`() = runTest {
        // GIVEN
        repository.addToWatchlist("AAPL")
        repository.addToWatchlist("MSFT")

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(2, result.size)
        assertEquals("AAPL", result[0].tickerId)
        assertEquals("MSFT", result[1].tickerId)
    }
}
