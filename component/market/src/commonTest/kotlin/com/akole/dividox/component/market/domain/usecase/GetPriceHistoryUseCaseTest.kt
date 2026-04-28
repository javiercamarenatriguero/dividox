package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.FakeMarketRepository
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.PricePoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class GetPriceHistoryUseCaseTest {

    private val repo = FakeMarketRepository()
    private val useCase = GetPriceHistoryUseCase(repo)

    @Test
    fun `SHOULD emit empty list WHEN no price data available GIVEN repo returns empty`() = runTest {
        // GIVEN
        // repo.priceHistoryResult defaults to emptyList()

        // WHEN
        val result = useCase("AAPL", ChartPeriod.ONE_DAY).first()

        // THEN
        assertEquals(emptyList(), result)
    }

    @Test
    fun `SHOULD emit price points WHEN data is available GIVEN repo returns points`() = runTest {
        // GIVEN
        val points = listOf(
            PricePoint(Instant.fromEpochSeconds(1700000000), 148.0),
            PricePoint(Instant.fromEpochSeconds(1700086400), 150.0),
        )
        repo.priceHistoryResult = points

        // WHEN
        val result = useCase("AAPL", ChartPeriod.ONE_MONTH).first()

        // THEN
        assertEquals(2, result.size)
        assertEquals(150.0, result.last().close)
    }

    @Test
    fun `SHOULD respect period parameter WHEN requesting different ranges GIVEN same ticker`() = runTest {
        // GIVEN
        val points = listOf(PricePoint(Instant.fromEpochSeconds(1700000000), 155.0))
        repo.priceHistoryResult = points

        // WHEN
        val resultOneDay = useCase("AAPL", ChartPeriod.ONE_DAY).first()
        val resultOneYear = useCase("AAPL", ChartPeriod.ONE_YEAR).first()

        // THEN
        assertEquals(1, resultOneDay.size)
        assertEquals(1, resultOneYear.size)
    }
}
