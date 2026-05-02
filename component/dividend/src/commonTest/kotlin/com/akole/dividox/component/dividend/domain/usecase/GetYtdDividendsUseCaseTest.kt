package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.FakeDividendRepository
import com.akole.dividox.component.dividend.domain.model.PaymentMethod
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.test.Test
import kotlin.test.assertEquals

class GetYtdDividendsUseCaseTest {

    private val repo = FakeDividendRepository()
    private val useCase = GetYtdDividendsUseCase(repo)

    @Test
    fun `SHOULD return zero WHEN no payments exist GIVEN empty repository`() = runTest {
        // GIVEN

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(0.0, result)
    }

    @Test
    fun `SHOULD include only current year CASH payments WHEN payments span multiple years`() = runTest {
        // GIVEN
        val currentYear = Clock.System.todayIn(TimeZone.UTC).year
        val thisYear = FakeDividendRepository.samplePayment(
            id = "p1",
            amount = 120.0,
            method = PaymentMethod.CASH,
            paymentDate = LocalDate(currentYear, 6, 15),
        )
        val lastYear = FakeDividendRepository.samplePayment(
            id = "p2",
            amount = 80.0,
            method = PaymentMethod.CASH,
            paymentDate = LocalDate(currentYear - 1, 12, 31),
        )
        repo.setPayments(listOf(thisYear, lastYear))

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(120.0, result, absoluteTolerance = 0.001)
    }
}
