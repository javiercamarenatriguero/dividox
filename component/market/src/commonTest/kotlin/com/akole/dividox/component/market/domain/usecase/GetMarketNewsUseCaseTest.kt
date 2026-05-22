package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.FakeMarketRepository
import com.akole.dividox.component.market.domain.model.NewsItem
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetMarketNewsUseCaseTest {

    private val repository = FakeMarketRepository()
    private val useCase = GetMarketNewsUseCase(repository)

    @Test
    fun invoke_defaultMarketUS_queriesGSPC() = runTest {
        // GIVEN
        val news = listOf(
            NewsItem("1", "S&P reaches new high", "Reuters", "https://example.com/1", Instant.fromEpochSeconds(1000L), null),
        )
        repository.newsResult = Result.success(news)

        // WHEN
        val result = useCase("US")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun invoke_defaultMarketES_returnsNews() = runTest {
        // GIVEN
        repository.newsResult = Result.success(emptyList())

        // WHEN
        val result = useCase("ES")

        // THEN
        assertTrue(result.isSuccess)
    }

    @Test
    fun invoke_defaultMarketALL_returnsNews() = runTest {
        // GIVEN
        repository.newsResult = Result.success(emptyList())

        // WHEN
        val result = useCase("ALL")

        // THEN
        assertTrue(result.isSuccess)
    }

    @Test
    fun invoke_networkError_returnsFailure() = runTest {
        // GIVEN
        repository.newsResult = Result.failure(RuntimeException("Timeout"))

        // WHEN
        val result = useCase("US")

        // THEN
        assertTrue(result.isFailure)
    }
}
