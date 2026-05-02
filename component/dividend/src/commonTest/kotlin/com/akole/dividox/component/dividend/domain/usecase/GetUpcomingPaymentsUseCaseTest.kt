package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.FakeDividendRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetUpcomingPaymentsUseCaseTest {

    private val repo = FakeDividendRepository()
    private val useCase = GetUpcomingPaymentsUseCase(repo)

    @Test
    fun `SHOULD return empty list WHEN no future payments GIVEN past payments only`() = runTest {
        // GIVEN
        val today = Clock.System.todayIn(TimeZone.UTC)
        val past = FakeDividendRepository.samplePayment(
            id = "p1",
            paymentDate = today.minus(1, DateTimeUnit.DAY),
        )
        repo.setPayments(listOf(past))

        // WHEN
        val result = useCase().first()

        // THEN
        assertTrue(result.isEmpty())
    }

    @Test
    fun `SHOULD return upcoming payment WHEN future payment exists GIVEN one future payment`() = runTest {
        // GIVEN
        val today = Clock.System.todayIn(TimeZone.UTC)
        val future = FakeDividendRepository.samplePayment(
            id = "p2",
            paymentDate = today.plus(7, DateTimeUnit.DAY),
        )
        repo.setPayments(listOf(future))

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(1, result.size)
        assertEquals("p2", result.first().id.value)
    }
}
