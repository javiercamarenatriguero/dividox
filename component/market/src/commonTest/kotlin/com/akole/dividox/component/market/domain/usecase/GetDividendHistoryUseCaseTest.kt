package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.FakeMarketRepository
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.MarketError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GetDividendHistoryUseCaseTest {

    private val repo = FakeMarketRepository()
    private val useCase = GetDividendHistoryUseCase(repo)

    @Test
    fun `SHOULD return empty list WHEN ticker has no dividend history GIVEN repo returns empty`() = runTest {
        // GIVEN
        // repo.dividendHistoryResult defaults to emptyList()

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `SHOULD return history entries WHEN ticker has dividends GIVEN repo returns data`() = runTest {
        // GIVEN
        val history = listOf(
            DividendInfo("AAPL", 0.5, 0.88, 0.15, 4.0, null),
            DividendInfo("AAPL", 0.6, 0.96, 0.16, 5.0, null),
        )
        repo.dividendHistoryResult = Result.success(history)

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `SHOULD return failure WHEN repo fails GIVEN RateLimited error`() = runTest {
        // GIVEN
        repo.dividendHistoryResult = Result.failure(MarketError.RateLimited)

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is MarketError.RateLimited)
    }
}
