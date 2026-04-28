package com.akole.dividox.component.portfolio.domain.usecase

import com.akole.dividox.component.portfolio.FakePortfolioRepository
import com.akole.dividox.component.portfolio.domain.model.Currency
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetPortfolioUseCaseTest {
    private val testRepository = FakePortfolioRepository()
    private val useCase = GetPortfolioUseCase(testRepository)

    @Test
    fun execute_emits_portfolio_success() = runTest {
        // GIVEN
        val holdings = listOf(
            Holding(
                id = HoldingId("1"),
                tickerId = "AAPL",
                shares = 10.0,
                purchasePrice = 150.0,
                purchaseCurrency = Currency.USD,
                purchaseDate = 1000L,
            ),
        )
        testRepository.portfolioResult = Result.success(holdings)

        // WHEN
        val results = useCase.execute().toList()

        // THEN
        assertEquals(1, results.size)
        assertTrue(results[0].isSuccess)
        assertEquals(holdings, results[0].getOrNull())
    }

    @Test
    fun execute_emits_error() = runTest {
        // GIVEN
        val exception = Exception("Firestore error")
        testRepository.portfolioResult = Result.failure(exception)

        // WHEN
        val results = useCase.execute().toList()

        // THEN
        assertEquals(1, results.size)
        assertTrue(results[0].isFailure)
        assertEquals(exception, results[0].exceptionOrNull())
    }
}
