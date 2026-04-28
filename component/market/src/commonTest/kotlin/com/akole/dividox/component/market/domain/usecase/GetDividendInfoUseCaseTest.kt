package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.FakeMarketRepository
import com.akole.dividox.component.market.domain.model.MarketError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDividendInfoUseCaseTest {

    private val repo = FakeMarketRepository()
    private val useCase = GetDividendInfoUseCase(repo)

    @Test
    fun `SHOULD return dividend info WHEN ticker is valid GIVEN successful repo response`() = runTest {
        // GIVEN
        // repo.dividendInfoResult is pre-set to success

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals("AAPL", result.getOrNull()?.ticker)
    }

    @Test
    fun `SHOULD return correct yield WHEN repo returns dividend info GIVEN valid ticker`() = runTest {
        // GIVEN
        // default dividendInfoResult has yield = 0.5

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertEquals(0.5, result.getOrNull()?.yield)
    }

    @Test
    fun `SHOULD return failure WHEN repo fails GIVEN RateLimited error`() = runTest {
        // GIVEN
        repo.dividendInfoResult = Result.failure(MarketError.RateLimited)

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is MarketError.RateLimited)
    }
}
