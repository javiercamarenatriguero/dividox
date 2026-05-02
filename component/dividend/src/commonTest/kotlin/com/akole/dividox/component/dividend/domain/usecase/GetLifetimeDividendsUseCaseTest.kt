package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.FakeDividendRepository
import com.akole.dividox.component.dividend.domain.model.PaymentMethod
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetLifetimeDividendsUseCaseTest {

    private val repo = FakeDividendRepository()
    private val useCase = GetLifetimeDividendsUseCase(repo)

    @Test
    fun `SHOULD return zero WHEN no payments exist GIVEN empty repository`() = runTest {
        // GIVEN
        // repo starts empty

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(0.0, result)
    }

    @Test
    fun `SHOULD sum only CASH payments WHEN mixed payment methods GIVEN payments with CASH and REINVESTED`() = runTest {
        // GIVEN
        val cash1 = FakeDividendRepository.samplePayment(id = "p1", amount = 100.0, method = PaymentMethod.CASH)
        val cash2 = FakeDividendRepository.samplePayment(id = "p2", amount = 50.0, method = PaymentMethod.CASH)
        val reinvested = FakeDividendRepository.samplePayment(id = "p3", amount = 200.0, method = PaymentMethod.REINVESTED)
        repo.setPayments(listOf(cash1, cash2, reinvested))

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(150.0, result, absoluteTolerance = 0.001)
    }
}
