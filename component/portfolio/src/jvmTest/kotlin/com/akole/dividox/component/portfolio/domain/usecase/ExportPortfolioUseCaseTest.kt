package com.akole.dividox.component.portfolio.domain.usecase

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportPortfolioUseCaseTest {

    private val useCase = ExportPortfolioUseCase()

    @Test
    fun invoke_empty_list_returns_header_only() {
        // GIVEN
        val holdings = emptyList<Holding>()

        // WHEN
        val result = useCase(holdings)

        // THEN
        assertEquals("Ticker,Shares,Purchase Price,Currency,Purchase Date", result)
    }

    @Test
    fun invoke_single_holding_returns_correct_row() {
        // GIVEN
        val holding = Holding(
            id = HoldingId("1"),
            tickerId = "AAPL",
            shares = 10.0,
            purchasePrice = 150.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 1_705_276_800_000L, // 2024-01-15 00:00:00 UTC
        )

        // WHEN
        val result = useCase(listOf(holding))

        // THEN
        val lines = result.lines()
        assertEquals(2, lines.size)
        assertEquals("Ticker,Shares,Purchase Price,Currency,Purchase Date", lines[0])
        assertEquals("AAPL,10.0,150.0,USD,2024-01-15", lines[1])
    }

    @Test
    fun invoke_multiple_holdings_returns_all_rows() {
        // GIVEN
        val holdings = listOf(
            Holding(
                id = HoldingId("1"),
                tickerId = "AAPL",
                shares = 10.0,
                purchasePrice = 150.0,
                purchaseCurrency = Currency.USD,
                purchaseDate = 1_705_276_800_000L,
            ),
            Holding(
                id = HoldingId("2"),
                tickerId = "MC.PA",
                shares = 5.5,
                purchasePrice = 700.0,
                purchaseCurrency = Currency.EUR,
                purchaseDate = 1_706_486_400_000L, // 2024-01-29 00:00:00 UTC
            ),
        )

        // WHEN
        val result = useCase(holdings)

        // THEN
        val lines = result.lines()
        assertEquals(3, lines.size)
        assertTrue(lines[1].startsWith("AAPL,"))
        assertTrue(lines[2].startsWith("MC.PA,"))
    }

    @Test
    fun invoke_formats_date_as_iso8601() {
        // GIVEN
        val holding = Holding(
            id = HoldingId("1"),
            tickerId = "T",
            shares = 1.0,
            purchasePrice = 20.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 1_672_531_200_000L, // 2023-01-01 00:00:00 UTC
        )

        // WHEN
        val result = useCase(listOf(holding))

        // THEN
        val row = result.lines()[1]
        assertTrue(row.endsWith("2023-01-01"), "Date should be ISO-8601, got: $row")
    }
}
