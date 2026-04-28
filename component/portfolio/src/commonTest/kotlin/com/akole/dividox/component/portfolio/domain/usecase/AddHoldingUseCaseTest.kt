package com.akole.dividox.component.portfolio.domain.usecase

import com.akole.dividox.component.portfolio.FakePortfolioRepository
import com.akole.dividox.component.portfolio.domain.model.Currency
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddHoldingUseCaseTest {
    private val testRepository = FakePortfolioRepository()
    private val useCase = AddHoldingUseCase(testRepository)

    @Test
    fun execute_adds_holding_and_returns_id() = runTest {
        // GIVEN
        val holding = Holding(
            id = HoldingId("temp"),
            tickerId = "MSFT",
            shares = 5.0,
            purchasePrice = 300.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 2000L,
        )
        val generatedId = HoldingId("generated-id-123")
        testRepository.addResult = Result.success(generatedId)

        // WHEN
        val result = useCase.execute(holding)

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(generatedId, result.getOrNull())
    }

    @Test
    fun execute_returns_error_on_failure() = runTest {
        // GIVEN
        val holding = Holding(
            id = HoldingId("temp"),
            tickerId = "TSLA",
            shares = 1.0,
            purchasePrice = 200.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 3000L,
        )
        val exception = Exception("Add failed")
        testRepository.addResult = Result.failure(exception)

        // WHEN
        val result = useCase.execute(holding)

        // THEN
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
