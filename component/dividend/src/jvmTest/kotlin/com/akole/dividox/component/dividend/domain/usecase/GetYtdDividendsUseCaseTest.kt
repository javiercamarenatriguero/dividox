package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetYtdDividendsUseCaseTest {

    private val repository = mockk<DividendRepository>()
    private val useCase = GetYtdDividendsUseCase(repository)

    @Test
    fun `SHOULD return zero WHEN no payments exist GIVEN empty repository`() = runTest {
        // GIVEN
        every { repository.getYtdDividends() } returns flowOf(0.0)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(0.0, result)
        verify { repository.getYtdDividends() }
    }

    @Test
    fun `SHOULD return ytd total WHEN repository emits a value GIVEN 120 year-to-date dividends`() = runTest {
        // GIVEN
        every { repository.getYtdDividends() } returns flowOf(120.0)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(120.0, result, absoluteTolerance = 0.001)
        verify { repository.getYtdDividends() }
    }
}
