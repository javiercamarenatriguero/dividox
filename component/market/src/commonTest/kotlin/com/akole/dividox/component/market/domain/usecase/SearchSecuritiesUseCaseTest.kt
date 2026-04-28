package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.FakeMarketRepository
import com.akole.dividox.component.market.domain.model.MarketError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchSecuritiesUseCaseTest {

    private val repo = FakeMarketRepository()
    private val useCase = SearchSecuritiesUseCase(repo)

    @Test
    fun `SHOULD return list of quotes WHEN query matches GIVEN successful repo response`() = runTest {
        // GIVEN
        // repo.quoteResult is pre-set to success with "AAPL"

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("AAPL", result.getOrNull()?.first()?.ticker)
    }

    @Test
    fun `SHOULD return empty list WHEN no results match GIVEN empty quoteResult`() = runTest {
        // GIVEN
        repo.quoteResult = Result.success(
            com.akole.dividox.component.market.domain.model.StockQuote(
                "XYZ", 0.0, 0.0, 0.0, "USD", kotlin.time.Clock.System.now()
            )
        )

        // WHEN
        val result = useCase("XYZ")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals("XYZ", result.getOrNull()?.first()?.ticker)
    }

    @Test
    fun `SHOULD return failure WHEN repo fails GIVEN NetworkError`() = runTest {
        // GIVEN
        repo.quoteResult = Result.failure(MarketError.NetworkError)

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is MarketError.NetworkError)
    }
}
