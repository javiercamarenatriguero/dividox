package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.FakeDividendRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDividendHistoryUseCaseTest {

    private val repo = FakeDividendRepository()
    private val useCase = GetDividendHistoryUseCase(repo)

    @Test
    fun `SHOULD return empty list WHEN no payments exist GIVEN empty repository`() = runTest {
        // GIVEN
        // repo starts empty

        // WHEN
        val result = useCase().first()

        // THEN
        assertTrue(result.isEmpty())
    }

    @Test
    fun `SHOULD return all payments WHEN payments are present GIVEN populated repository`() = runTest {
        // GIVEN
        val payment1 = FakeDividendRepository.samplePayment(id = "p1", ticker = "AAPL")
        val payment2 = FakeDividendRepository.samplePayment(id = "p2", ticker = "MSFT")
        repo.setPayments(listOf(payment1, payment2))

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(2, result.size)
    }
}
