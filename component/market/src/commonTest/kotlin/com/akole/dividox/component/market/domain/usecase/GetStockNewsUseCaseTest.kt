package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.FakeMarketRepository
import com.akole.dividox.component.market.domain.model.NewsItem
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetStockNewsUseCaseTest {

    private val repository = FakeMarketRepository()
    private val useCase = GetStockNewsUseCase(repository)

    @Test
    fun invoke_happyPath_returnsMappedNews() = runTest {
        // GIVEN
        val news = listOf(
            NewsItem("1", "AAPL hits record high", "Bloomberg", "https://example.com/1", Instant.fromEpochSeconds(1000L), null),
        )
        repository.newsResult = Result.success(news)

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("AAPL hits record high", result.getOrNull()?.first()?.title)
    }

    @Test
    fun invoke_emptyList_returnsEmpty() = runTest {
        // GIVEN
        repository.newsResult = Result.success(emptyList())

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun invoke_networkError_returnsFailure() = runTest {
        // GIVEN
        repository.newsResult = Result.failure(RuntimeException("Network error"))

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isFailure)
    }
}
