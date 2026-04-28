package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.FakeMarketRepository
import com.akole.dividox.component.market.domain.model.MarketError
import com.akole.dividox.component.market.domain.model.StockQuote
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest

class GetMultipleQuotesUseCaseTest {

    private val repo = FakeMarketRepository()
    private val useCase = GetMultipleQuotesUseCase(repo)

    @Test
    fun `SHOULD return list of quotes WHEN tickers are valid GIVEN successful repo response`() = runTest {
        // GIVEN
        val quotes = listOf(
            StockQuote("AAPL", 150.0, 1.5, 1.0, "USD", Clock.System.now()),
            StockQuote("MSFT", 420.0, 2.0, 0.5, "USD", Clock.System.now()),
        )
        repo.multipleQuotesResult = Result.success(quotes)

        // WHEN
        val result = useCase(listOf("AAPL", "MSFT"))

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("AAPL", result.getOrNull()?.first()?.ticker)
    }

    @Test
    fun `SHOULD return empty list WHEN no tickers given GIVEN repo returns empty`() = runTest {
        // GIVEN
        repo.multipleQuotesResult = Result.success(emptyList())

        // WHEN
        val result = useCase(emptyList())

        // THEN
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `SHOULD return failure WHEN repo fails GIVEN NetworkError`() = runTest {
        // GIVEN
        repo.multipleQuotesResult = Result.failure(MarketError.NetworkError)

        // WHEN
        val result = useCase(listOf("AAPL", "MSFT"))

        // THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is MarketError.NetworkError)
    }
}
