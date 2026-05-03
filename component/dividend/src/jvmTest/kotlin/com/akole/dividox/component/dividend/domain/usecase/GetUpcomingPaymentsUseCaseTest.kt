package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
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

class GetUpcomingPaymentsUseCaseTest {

    private val repository = mockk<DividendRepository>()
    private val useCase = GetUpcomingPaymentsUseCase(repository)

    @Test
    fun `SHOULD return empty list WHEN no upcoming payments GIVEN empty repository`() = runTest {
        // GIVEN
        every { repository.getUpcomingPayments() } returns flowOf(emptyList())

        // WHEN
        val result = useCase().first()

        // THEN
        assertTrue(result.isEmpty())
        verify { repository.getUpcomingPayments() }
    }

    @Test
    fun `SHOULD return upcoming payment WHEN future payment exists GIVEN one future payment`() = runTest {
        // GIVEN
        val upcoming = DividendPayment(
            id = DividendPaymentId("p1"),
            tickerId = "AAPL",
            amount = 50.0,
            currency = "USD",
            paymentDate = LocalDate(2026, 12, 31),
        )
        every { repository.getUpcomingPayments() } returns flowOf(listOf(upcoming))

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(1, result.size)
        assertEquals("p1", result.first().id.value)
        verify { repository.getUpcomingPayments() }
    }
}
