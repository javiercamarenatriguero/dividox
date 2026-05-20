package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.FakeMarketRepository
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetMajorMarketIndicesUseCaseTest {
    private val repo = FakeMarketRepository()
    private val useCase = GetMajorMarketIndicesUseCase(repo)

    @Test
    fun `SHOULD return indices in default order WHEN defaultMarket is ALL GIVEN successful repo response`() =
        kotlinx.coroutines.runBlocking {
            // GIVEN
            val now = kotlin.time.Clock.System.now()
            val quotes = listOf(
                StockQuote("^IXIC", 18500.0, 100.0, 0.54, "USD", now, name = "NASDAQ"),
                StockQuote("^STOXX50E", 5200.0, 50.0, 0.97, "EUR", now, name = "EURO STOXX 50"),
                StockQuote("^IBEX", 11000.0, 10.0, 0.09, "EUR", now, name = "IBEX 35"),
                StockQuote("^GDAXI", 18200.0, 150.0, 0.83, "EUR", now, name = "DAX"),
                StockQuote("^N225", 33500.0, 200.0, 0.60, "JPY", now, name = "NIKKEI"),
                StockQuote("^FTSE", 8100.0, 50.0, 0.62, "GBP", now, name = "FTSE 100"),
            )
            repo.multipleQuotesResult = Result.success(quotes)

            // WHEN
            val result = useCase("ALL")

            // THEN
            assertTrue(result.isSuccess)
            val indices = result.getOrNull()!!
            assertEquals(6, indices.size)
            assertEquals("^IXIC", indices[0].ticker) // Nasdaq first in catalog
        }

    @Test
    fun `SHOULD return selected market first WHEN defaultMarket is DE GIVEN successful repo response`() =
        kotlinx.coroutines.runBlocking {
            // GIVEN
            val now = kotlin.time.Clock.System.now()
            val quotes = listOf(
                StockQuote("^IXIC", 18500.0, 100.0, 0.54, "USD", now),
                StockQuote("^STOXX50E", 5200.0, 50.0, 0.97, "EUR", now),
                StockQuote("^IBEX", 11000.0, 10.0, 0.09, "EUR", now),
                StockQuote("^GDAXI", 18200.0, 150.0, 0.83, "EUR", now),
                StockQuote("^N225", 33500.0, 200.0, 0.60, "JPY", now),
                StockQuote("^FTSE", 8100.0, 50.0, 0.62, "GBP", now),
            )
            repo.multipleQuotesResult = Result.success(quotes)

            // WHEN
            val result = useCase("DE")

            // THEN
            assertTrue(result.isSuccess)
            val indices = result.getOrNull()!!
            assertEquals("^GDAXI", indices[0].ticker) // DAX first (DE market)
            assertEquals("DE", indices[0].marketKey)
        }

    @Test
    fun `SHOULD return partial success WHEN some tickers fail GIVEN partial repo response`() =
        kotlinx.coroutines.runBlocking {
            // GIVEN
            val now = kotlin.time.Clock.System.now()
            val quotes = listOf(
                StockQuote("^IXIC", 18500.0, 100.0, 0.54, "USD", now),
                StockQuote("^GDAXI", 18200.0, 150.0, 0.83, "EUR", now),
                StockQuote("^FTSE", 8100.0, 50.0, 0.62, "GBP", now),
                StockQuote("^N225", 33500.0, 200.0, 0.60, "JPY", now),
            )
            repo.multipleQuotesResult = Result.success(quotes)

            // WHEN
            val result = useCase("ALL")

            // THEN
            assertTrue(result.isSuccess)
            val indices = result.getOrNull()!!
            assertEquals(4, indices.size)
        }

    @Test
    fun `SHOULD return failure WHEN all tickers fail GIVEN empty repo response`() =
        kotlinx.coroutines.runBlocking {
            // GIVEN
            repo.multipleQuotesResult = Result.success(emptyList<StockQuote>())

            // WHEN
            val result = useCase("ALL")

            // THEN
            assertTrue(result.isFailure)
        }

    @Test
    fun `SHOULD map StockQuote fields to MarketIndexQuote correctly`() =
        kotlinx.coroutines.runBlocking {
            // GIVEN
            val now = kotlin.time.Clock.System.now()
            val quotes = listOf(
                StockQuote("^IXIC", 18500.123, 100.456, 0.5432, "USD", now),
            )
            repo.multipleQuotesResult = Result.success(quotes)

            // WHEN
            val result = useCase("ALL")

            // THEN
            assertTrue(result.isSuccess)
            val index = result.getOrNull()!![0]
            assertEquals("Nasdaq Composite", index.name)
            assertEquals("^IXIC", index.ticker)
            assertEquals("US", index.marketKey)
            assertEquals(18500.123, index.points)
            assertEquals(100.456, index.changePoints)
            assertEquals(0.5432, index.changePercent)
        }
}
