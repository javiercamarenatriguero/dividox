package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.FakeDividendRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AddDividendPaymentUseCaseTest {

    private val repo = FakeDividendRepository()
    private val useCase = AddDividendPaymentUseCase(repo)

    @Test
    fun `SHOULD add payment WHEN invoked GIVEN valid payment`() = runTest {
        // GIVEN
        val payment = FakeDividendRepository.samplePayment(id = "new")

        // WHEN
        useCase(payment)

        // THEN
        val history = repo.getDividendHistory().first()
        assertEquals(1, history.size)
        assertEquals("new", history.first().id.value)
    }
}
