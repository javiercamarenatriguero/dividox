package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.FakeMarketRepository
import com.akole.dividox.component.market.domain.model.MarketError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetStockQuoteUseCaseTest {

    private val repo = FakeMarketRepository()
    private val useCase = GetStockQuoteUseCase(repo)

    @Test
    fun `SHOULD return quote on success WHEN ticker is valid GIVEN successful repo response`() = runTest {
        // GIVEN
        // repo.quoteResult is pre-set to success with "AAPL"

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals("AAPL", result.getOrNull()?.ticker)
    }

    @Test
    fun `SHOULD return failure WHEN repo fails GIVEN NotFound error`() = runTest {
        // GIVEN
        repo.quoteResult = Result.failure(MarketError.NotFound("AAPL"))

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is MarketError.NotFound)
    }

    @Test
    fun `SHOULD propagate price WHEN repo returns a quote GIVEN valid ticker`() = runTest {
        // GIVEN
        // default quoteResult has price = 150.0

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertEquals(150.0, result.getOrNull()?.price)
    }
}
