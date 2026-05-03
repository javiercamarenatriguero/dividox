package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class AddDividendPaymentUseCaseTest {

    private val repository = mockk<DividendRepository>()
    private val useCase = AddDividendPaymentUseCase(repository)

    @Test
    fun `SHOULD delegate to repository WHEN invoked GIVEN valid payment`() = runTest {
        // GIVEN
        val payment = DividendPayment(
            id = DividendPaymentId("p1"),
            tickerId = "AAPL",
            amount = 100.0,
            currency = "USD",
            paymentDate = LocalDate(2025, 3, 15),
        )
        coEvery { repository.addDividendPayment(payment) } just Runs

        // WHEN
        useCase(payment)

        // THEN
        coVerify(exactly = 1) { repository.addDividendPayment(payment) }
    }
}
