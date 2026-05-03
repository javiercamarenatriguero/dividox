package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.component.dividend.domain.model.PaymentMethod
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDividendHistoryUseCaseTest {

    private val repository = mockk<DividendRepository>()
    private val useCase = GetDividendHistoryUseCase(repository)

    @Test
    fun `SHOULD return empty list WHEN no payments exist GIVEN empty repository`() = runTest {
        // GIVEN
        every { repository.getDividendHistory() } returns flowOf(emptyList())

        // WHEN
        val result = useCase().first()

        // THEN
        assertTrue(result.isEmpty())
        verify { repository.getDividendHistory() }
    }

    @Test
    fun `SHOULD return all payments WHEN payments are present GIVEN populated repository`() = runTest {
        // GIVEN
        val payments = listOf(
            samplePayment(id = "p1", ticker = "AAPL"),
            samplePayment(id = "p2", ticker = "MSFT"),
        )
        every { repository.getDividendHistory() } returns flowOf(payments)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(2, result.size)
        verify { repository.getDividendHistory() }
    }

    private fun samplePayment(id: String, ticker: String) = DividendPayment(
        id = DividendPaymentId(id),
        tickerId = ticker,
        amount = 100.0,
        currency = "USD",
        paymentDate = LocalDate(2025, 3, 15),
        method = PaymentMethod.CASH,
    )
}
