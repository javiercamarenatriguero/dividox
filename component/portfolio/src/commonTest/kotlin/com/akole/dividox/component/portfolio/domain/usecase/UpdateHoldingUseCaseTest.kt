package com.akole.dividox.component.portfolio.domain.usecase

import com.akole.dividox.component.portfolio.FakePortfolioRepository
import com.akole.dividox.common.ui.resources.Currency
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class UpdateHoldingUseCaseTest {
    private val testRepository = FakePortfolioRepository()
    private val useCase = UpdateHoldingUseCase(testRepository)

    @Test
    fun execute_updates_holding() = runTest {
        // GIVEN
        val holding = Holding(
            id = HoldingId("hold-1"),
            tickerId = "GOOGL",
            shares = 20.0,
            purchasePrice = 140.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 4000L,
        )
        testRepository.updateResult = Result.success(Unit)

        // WHEN
        val result = useCase.execute(holding)

        // THEN
        assertTrue(result.isSuccess)
    }

    @Test
    fun execute_returns_error() = runTest {
        // GIVEN
        val holding = Holding(
            id = HoldingId("hold-2"),
            tickerId = "AMZN",
            shares = 15.0,
            purchasePrice = 170.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 5000L,
        )
        val exception = Exception("Update failed")
        testRepository.updateResult = Result.failure(exception)

        // WHEN
        val result = useCase.execute(holding)

        // THEN
        assertTrue(result.isFailure)
    }
}
