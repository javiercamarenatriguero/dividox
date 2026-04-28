package com.akole.dividox.component.portfolio.domain.usecase

import com.akole.dividox.component.portfolio.FakePortfolioRepository
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class RemoveHoldingUseCaseTest {
    private val testRepository = FakePortfolioRepository()
    private val useCase = RemoveHoldingUseCase(testRepository)

    @Test
    fun execute_removes_holding() = runTest {
        // GIVEN
        val holdingId = HoldingId("hold-to-remove")
        testRepository.removeResult = Result.success(Unit)

        // WHEN
        val result = useCase.execute(holdingId)

        // THEN
        assertTrue(result.isSuccess)
    }

    @Test
    fun execute_returns_error() = runTest {
        // GIVEN
        val holdingId = HoldingId("missing-holding")
        val exception = Exception("Holding not found")
        testRepository.removeResult = Result.failure(exception)

        // WHEN
        val result = useCase.execute(holdingId)

        // THEN
        assertTrue(result.isFailure)
    }
}
