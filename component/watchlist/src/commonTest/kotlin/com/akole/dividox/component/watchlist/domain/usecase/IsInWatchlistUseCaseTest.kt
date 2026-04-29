package com.akole.dividox.component.watchlist.domain.usecase

import com.akole.dividox.component.watchlist.FakeWatchlistRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsInWatchlistUseCaseTest {

    private val repository = FakeWatchlistRepository()
    private val useCase = IsInWatchlistUseCase(repository)

    @Test
    fun `SHOULD emit false WHEN ticker not in watchlist GIVEN empty watchlist`() = runTest {
        // GIVEN — empty repository

        // WHEN
        val result = useCase("AAPL").first()

        // THEN
        assertFalse(result)
    }

    @Test
    fun `SHOULD emit true WHEN ticker is in watchlist GIVEN ticker was added`() = runTest {
        // GIVEN
        repository.addToWatchlist("AAPL")

        // WHEN
        val result = useCase("AAPL").first()

        // THEN
        assertTrue(result)
    }

    @Test
    fun `SHOULD emit false WHEN different ticker queried GIVEN watchlist has other tickers`() = runTest {
        // GIVEN
        repository.addToWatchlist("MSFT")

        // WHEN
        val result = useCase("AAPL").first()

        // THEN
        assertFalse(result)
    }

    @Test
    fun `SHOULD emit false WHEN ticker removed from watchlist GIVEN ticker was previously added`() = runTest {
        // GIVEN
        repository.addToWatchlist("TSLA")
        repository.removeFromWatchlist("TSLA")

        // WHEN
        val result = useCase("TSLA").first()

        // THEN
        assertFalse(result)
    }
}
