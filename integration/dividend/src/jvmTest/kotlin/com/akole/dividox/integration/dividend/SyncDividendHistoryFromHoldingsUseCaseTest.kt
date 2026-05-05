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

    private val marketRepository = mockk<MarketRepository>()
    private val dividendRepository = mockk<DividendRepository>(relaxed = true)

    private val useCase = SyncDividendHistoryFromHoldingsUseCase(
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
    fun `empty portfolio does not call replaceAllPayments`() = runTest {
        // WHEN
        useCase(emptyList())

        // THEN — returns early; cache untouched
        coVerify(exactly = 0) { dividendRepository.replaceAllPayments(any()) }
    }

    @Test
    fun `market API failure for ticker replaces cache with empty list`() = runTest {
        // GIVEN — API fails; no eligible payments collected
        val holdings = listOf(holding("AAPL", shares = 10.0))
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.failure(RuntimeException("timeout"))

        // WHEN
        val result = useCase(holdings)

        // THEN — sync completes; stale cache cleared
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { dividendRepository.replaceAllPayments(emptyList()) }
    }

    @Test
    fun `persists payment with correct amount and stable ID`() = runTest {
        // GIVEN
        val purchaseDate = LocalDate(2024, 1, 1)
        val exDate = LocalDate(2024, 3, 15)
        val shares = 10.0
        val amountPerShare = 0.25
        val holdings = listOf(holding("AAPL", shares = shares, purchaseDateEpochMs = purchaseDate.toEpochMs()))
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", amountPerShare, exDate)))
        val slot = slot<List<DividendPayment>>()
        coEvery { dividendRepository.replaceAllPayments(capture(slot)) } returns Unit

        // WHEN
        useCase(holdings)

        // THEN
        val persisted = slot.captured.single()
        assertEquals("AAPL-$exDate", persisted.id.value)
        assertEquals("AAPL", persisted.tickerId)
        assertEquals(shares * amountPerShare, persisted.amount)
        assertEquals(exDate, persisted.paymentDate)
        assertEquals("USD", persisted.currency)
    }

    @Test
    fun `dividend event before purchase date is not persisted`() = runTest {
        // GIVEN — ex-date is before the earliest purchase date for the ticker
        val exDate = LocalDate(2023, 6, 15)
        val purchaseDate = LocalDate(2024, 1, 1)
        val holdings = listOf(holding("AAPL", shares = 10.0, purchaseDateEpochMs = purchaseDate.toEpochMs()))
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", 0.25, exDate)))
        val slot = slot<List<DividendPayment>>()
        coEvery { dividendRepository.replaceAllPayments(capture(slot)) } returns Unit

        // WHEN
        useCase(holdings)

        // THEN — cache replaced with empty list (pre-purchase dividend excluded)
        assertTrue(slot.captured.isEmpty())
    }

    @Test
    fun `dividend event on same day as purchase date is not persisted`() = runTest {
        // GIVEN — buying on ex-date does not qualify per stock market convention
        val exDate = LocalDate(2024, 3, 15)
        val holdings = listOf(holding("AAPL", shares = 10.0, purchaseDateEpochMs = exDate.toEpochMs()))
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", 0.25, exDate)))
        val slot = slot<List<DividendPayment>>()
        coEvery { dividendRepository.replaceAllPayments(capture(slot)) } returns Unit

        // WHEN
        useCase(holdings)

        // THEN
        assertTrue(slot.captured.isEmpty())
    }

    @Test
    fun `two lots of same ticker — only lot purchased before ex-date counts`() = runTest {
        // GIVEN — lot1 purchased 2023-01-01, lot2 purchased 2024-01-01
        // ex-date 2023-06-15: lot1 is before ex-date (qualifies), lot2 is after (excluded)
        val exDate = LocalDate(2023, 6, 15)
        val lot1 = holding("AAPL", shares = 10.0, purchaseDateEpochMs = LocalDate(2023, 1, 1).toEpochMs())
        val lot2 = holding("AAPL", shares = 5.0, purchaseDateEpochMs = LocalDate(2024, 1, 1).toEpochMs())
        val amountPerShare = 0.25
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", amountPerShare, exDate)))
        val slot = slot<List<DividendPayment>>()
        coEvery { dividendRepository.replaceAllPayments(capture(slot)) } returns Unit

        // WHEN
        useCase(listOf(lot1, lot2))

        // THEN — only lot1 shares (10) count for this ex-date; lot2 bought later is excluded
        assertEquals(10.0 * amountPerShare, slot.captured.single().amount)
    }

    @Test
    fun `two lots of same ticker — both lots count when both predate ex-date`() = runTest {
        // GIVEN — both lots purchased before ex-date
        val exDate = LocalDate(2025, 3, 15)
        val lot1 = holding("AAPL", shares = 10.0, purchaseDateEpochMs = LocalDate(2023, 1, 1).toEpochMs())
        val lot2 = holding("AAPL", shares = 5.0, purchaseDateEpochMs = LocalDate(2024, 1, 1).toEpochMs())
        val amountPerShare = 0.25
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", amountPerShare, exDate)))
        val slot = slot<List<DividendPayment>>()
        coEvery { dividendRepository.replaceAllPayments(capture(slot)) } returns Unit

        // WHEN
        useCase(listOf(lot1, lot2))

        // THEN — all 15 shares qualify
        assertEquals(15.0 * amountPerShare, slot.captured.single().amount)
    }

    @Test
    fun `multiple events for same ticker are each included`() = runTest {
        // GIVEN — two dividend events after purchase date
        val purchaseDate = LocalDate(2024, 1, 1)
        val exDate1 = LocalDate(2024, 3, 15)
        val exDate2 = LocalDate(2024, 6, 15)
        val holdings = listOf(holding("AAPL", shares = 10.0, purchaseDateEpochMs = purchaseDate.toEpochMs()))
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(
            listOf(event("AAPL", 0.25, exDate1), event("AAPL", 0.25, exDate2)),
        )
        val slot = slot<List<DividendPayment>>()
        coEvery { dividendRepository.replaceAllPayments(capture(slot)) } returns Unit

        // WHEN
        useCase(holdings)

        // THEN — both events included
        assertEquals(2, slot.captured.size)
        assertTrue(slot.captured.any { it.id.value == "AAPL-$exDate1" })
        assertTrue(slot.captured.any { it.id.value == "AAPL-$exDate2" })
    }

    @Test
    fun `multiple tickers each call market API with MAX range`() = runTest {
        // GIVEN
        val holdings = listOf(
            holding("AAPL", shares = 10.0),
            holding("MSFT", shares = 5.0),
        )
        coEvery {
            marketRepository.getHistoricalDividendEvents(any(), DividendHistoryRange.MAX)
        } returns Result.success(emptyList())

        // WHEN
        useCase(holdings)

        // THEN
        coVerify(exactly = 1) { marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX) }
        coVerify(exactly = 1) { marketRepository.getHistoricalDividendEvents("MSFT", DividendHistoryRange.MAX) }
    }

    @Test
    fun `one ticker API failure does not prevent other tickers from syncing`() = runTest {
        // GIVEN — MSFT fails, AAPL succeeds
        val purchaseDate = LocalDate(2024, 1, 1)
        val exDate = LocalDate(2024, 3, 15)
        val holdings = listOf(
            holding("AAPL", shares = 10.0, purchaseDateEpochMs = purchaseDate.toEpochMs()),
            holding("MSFT", shares = 5.0, purchaseDateEpochMs = purchaseDate.toEpochMs()),
        )
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", 0.25, exDate)))
        coEvery {
            marketRepository.getHistoricalDividendEvents("MSFT", DividendHistoryRange.MAX)
        } returns Result.failure(RuntimeException("API error"))
        val slot = slot<List<DividendPayment>>()
        coEvery { dividendRepository.replaceAllPayments(capture(slot)) } returns Unit

        // WHEN
        val result = useCase(holdings)

        // THEN — AAPL payment included; MSFT skipped; overall success
        assertTrue(result.isSuccess)
        assertEquals(1, slot.captured.size)
        assertEquals("AAPL", slot.captured.single().tickerId)
    }

    @Test
    fun `re-sync produces same stable IDs so replace is idempotent`() = runTest {
        // GIVEN
        val purchaseDate = LocalDate(2024, 1, 1)
        val exDate = LocalDate(2024, 3, 15)
        val holdings = listOf(holding("AAPL", shares = 10.0, purchaseDateEpochMs = purchaseDate.toEpochMs()))
        coEvery {
            marketRepository.getHistoricalDividendEvents("AAPL", DividendHistoryRange.MAX)
        } returns Result.success(listOf(event("AAPL", 0.25, exDate)))
        coEvery { dividendRepository.replaceAllPayments(any()) } returns Unit

        // WHEN — sync twice
        useCase(holdings)
        useCase(holdings)

        // THEN — replaceAllPayments called twice with the same stable ID
        val expectedId = DividendPaymentId("AAPL-$exDate")
        coVerify(exactly = 2) {
            dividendRepository.replaceAllPayments(match { it.single().id == expectedId })
        }
    }
}

