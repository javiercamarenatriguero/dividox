package com.akole.dividox.integration.dividend

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.component.market.domain.model.MarketDividendEvent
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository
import com.akole.dividox.integration.dividend.domain.usecase.SyncDividendHistoryFromHoldingsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncDividendHistoryFromHoldingsUseCaseTest {

    private val portfolioRepository = mockk<PortfolioRepository>()
    private val marketRepository = mockk<MarketRepository>()
    private val dividendRepository = mockk<DividendRepository>(relaxed = true)

    private val useCase = SyncDividendHistoryFromHoldingsUseCase(
        portfolioRepository = portfolioRepository,
        marketRepository = marketRepository,
        dividendRepository = dividendRepository,
    )

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun holding(ticker: String, shares: Double, purchaseDateEpochMs: Long = 0L) = Holding(
        id = HoldingId("id-$ticker"),
        tickerId = ticker,
        shares = shares,
        purchasePrice = 100.0,
        purchaseCurrency = Currency.USD,
        purchaseDate = purchaseDateEpochMs,
    )

    private fun LocalDate.toEpochMs(): Long = toEpochDays().toLong() * 86_400_000L

    private fun event(ticker: String, amountPerShare: Double, exDate: LocalDate) =
        MarketDividendEvent(
            ticker = ticker,
            amountPerShare = amountPerShare,
            exDividendDate = exDate,
            currency = "USD",
        )

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    fun `empty portfolio persists nothing`() = runTest {
        // GIVEN
        coEvery { portfolioRepository.getPortfolio() } returns Result.success(emptyList())

        // WHEN
        useCase()

        // THEN
        coVerify(exactly = 0) { dividendRepository.addDividendPayment(any()) }
    }

    @Test
    fun `portfolio fetch failure returns success without persisting`() = runTest {
        // GIVEN
        coEvery { portfolioRepository.getPortfolio() } returns Result.failure(RuntimeException("network"))

        // WHEN
        val result = useCase()

        // THEN
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { dividendRepository.addDividendPayment(any()) }
    }

    @Test
    fun `market API failure for ticker is skipped silently`() = runTest {
        // GIVEN
        coEvery { portfolioRepository.getPortfolio() } returns Result.success(
            listOf(holding("AAPL", shares = 10.0)),
        )
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.failure(RuntimeException("timeout"))

        // WHEN
        val result = useCase()

        // THEN
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { dividendRepository.addDividendPayment(any()) }
    }

    @Test
    fun `persists payment with correct amount and stable ID`() = runTest {
        // GIVEN
        val exDate = LocalDate(2024, 3, 15)
        val shares = 10.0
        val amountPerShare = 0.25
        coEvery { portfolioRepository.getPortfolio() } returns Result.success(
            listOf(holding("AAPL", shares = shares)),
        )
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", amountPerShare, exDate)))
        val slot = slot<DividendPayment>()
        coEvery { dividendRepository.addDividendPayment(capture(slot)) } returns Unit

        // WHEN
        useCase()

        // THEN
        val persisted = slot.captured
        assertEquals("AAPL-$exDate", persisted.id.value)
        assertEquals("AAPL", persisted.tickerId)
        assertEquals(shares * amountPerShare, persisted.amount)
        assertEquals(exDate, persisted.paymentDate)
        assertEquals("USD", persisted.currency)
    }

    @Test
    fun `dividend event before purchase date is still persisted (no lot filter in MVP)`() = runTest {
        // GIVEN — ex-date is before the purchase date; MVP includes all historical dividends
        val exDate = LocalDate(2023, 6, 15)
        val purchaseDate = LocalDate(2024, 1, 1) // purchased after ex-date
        coEvery { portfolioRepository.getPortfolio() } returns Result.success(
            listOf(holding("AAPL", shares = 10.0, purchaseDateEpochMs = purchaseDate.toEpochMs())),
        )
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", 0.25, exDate)))
        coEvery { dividendRepository.addDividendPayment(any()) } returns Unit

        // WHEN
        useCase()

        // THEN — payment is persisted regardless of purchaseDate
        coVerify(exactly = 1) { dividendRepository.addDividendPayment(any()) }
    }

    @Test
    fun `dividend event on same day as purchase date is still persisted (no lot filter in MVP)`() = runTest {
        // GIVEN
        val exDate = LocalDate(2024, 3, 15)
        coEvery { portfolioRepository.getPortfolio() } returns Result.success(
            listOf(holding("AAPL", shares = 10.0, purchaseDateEpochMs = exDate.toEpochMs())),
        )
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", 0.25, exDate)))
        coEvery { dividendRepository.addDividendPayment(any()) } returns Unit

        // WHEN
        useCase()

        // THEN
        coVerify(exactly = 1) { dividendRepository.addDividendPayment(any()) }
    }

    @Test
    fun `two lots of same ticker sum all shares for every event`() = runTest {
        // GIVEN — total shares: 10 + 5 = 15, regardless of lot purchase dates
        val exDate = LocalDate(2024, 3, 15)
        val lot1 = holding("AAPL", shares = 10.0, purchaseDateEpochMs = LocalDate(2023, 1, 1).toEpochMs())
        val lot2 = holding("AAPL", shares = 5.0, purchaseDateEpochMs = LocalDate(2024, 1, 1).toEpochMs())
        val amountPerShare = 0.25
        coEvery { portfolioRepository.getPortfolio() } returns Result.success(listOf(lot1, lot2))
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", amountPerShare, exDate)))
        val slot = slot<DividendPayment>()
        coEvery { dividendRepository.addDividendPayment(capture(slot)) } returns Unit

        // WHEN
        useCase()

        // THEN — 10 + 5 = 15 shares × 0.25
        assertEquals(15.0 * amountPerShare, slot.captured.amount)
    }

    @Test
    fun `multiple events for same ticker are each persisted with correct amounts`() = runTest {
        // GIVEN — two dividend events in different quarters
        val exDate1 = LocalDate(2024, 3, 15)
        val exDate2 = LocalDate(2024, 6, 15)
        val shares = 10.0
        val amountPerShare = 0.25
        coEvery { portfolioRepository.getPortfolio() } returns Result.success(
            listOf(holding("AAPL", shares = shares)),
        )
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(
            listOf(
                event("AAPL", amountPerShare, exDate1),
                event("AAPL", amountPerShare, exDate2),
            ),
        )
        coEvery { dividendRepository.addDividendPayment(any()) } returns Unit

        // WHEN
        useCase()

        // THEN — one call per event
        coVerify(exactly = 1) {
            dividendRepository.addDividendPayment(match { it.id.value == "AAPL-$exDate1" })
        }
        coVerify(exactly = 1) {
            dividendRepository.addDividendPayment(match { it.id.value == "AAPL-$exDate2" })
        }
    }

    @Test
    fun `multiple tickers each call market API with MAX range`() = runTest {
        // GIVEN
        coEvery { portfolioRepository.getPortfolio() } returns Result.success(
            listOf(
                holding("AAPL", shares = 10.0),
                holding("MSFT", shares = 5.0),
            ),
        )
        coEvery {
            marketRepository.getHistoricalDividendEvents(any(), DividendHistoryRange.MAX)
        } returns Result.success(emptyList())

        // WHEN
        useCase()

        // THEN
        coVerify(exactly = 1) { marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX) }
        coVerify(exactly = 1) { marketRepository.getHistoricalDividendEvents("MSFT", DividendHistoryRange.MAX) }
    }

    @Test
    fun `one ticker API failure does not prevent other tickers from syncing`() = runTest {
        // GIVEN — MSFT fails, AAPL succeeds
        val exDate = LocalDate(2024, 3, 15)
        coEvery { portfolioRepository.getPortfolio() } returns Result.success(
            listOf(
                holding("AAPL", shares = 10.0),
                holding("MSFT", shares = 5.0),
            ),
        )
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", 0.25, exDate)))
        coEvery {
            marketRepository.getHistoricalDividendEvents("MSFT", DividendHistoryRange.MAX)
        } returns Result.failure(RuntimeException("API error"))
        coEvery { dividendRepository.addDividendPayment(any()) } returns Unit

        // WHEN
        val result = useCase()

        // THEN — AAPL persisted, MSFT skipped, overall result is success
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { dividendRepository.addDividendPayment(match { it.tickerId == "AAPL" }) }
        coVerify(exactly = 0) { dividendRepository.addDividendPayment(match { it.tickerId == "MSFT" }) }
    }

    @Test
    fun `re-sync uses same stable ID so upsert is idempotent`() = runTest {
        // GIVEN
        val exDate = LocalDate(2024, 3, 15)
        coEvery { portfolioRepository.getPortfolio() } returns Result.success(
            listOf(holding("AAPL", shares = 10.0)),
        )
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", 0.25, exDate)))
        coEvery { dividendRepository.addDividendPayment(any()) } returns Unit

        // WHEN — sync twice
        useCase()
        useCase()

        // THEN — same stable ID both times → upsert handles deduplication
        val expectedId = DividendPaymentId("AAPL-$exDate")
        coVerify(exactly = 2) {
            dividendRepository.addDividendPayment(match { it.id == expectedId })
        }
    }
}

