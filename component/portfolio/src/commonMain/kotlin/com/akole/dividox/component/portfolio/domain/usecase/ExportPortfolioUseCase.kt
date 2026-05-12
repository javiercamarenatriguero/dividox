package com.akole.dividox.component.portfolio.domain.usecase

import com.akole.dividox.component.portfolio.domain.model.Holding
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val CSV_HEADER = "Ticker,Shares,Purchase Price,Currency,Purchase Date"

class ExportPortfolioUseCase {
    operator fun invoke(holdings: List<Holding>): String {
        val rows = holdings.map { it.toCsvRow() }
        return (listOf(CSV_HEADER) + rows).joinToString("\n")
    }
}

private fun Holding.toCsvRow(): String {
    val date = Instant.fromEpochMilliseconds(purchaseDate)
        .toLocalDateTime(TimeZone.UTC)
        .date
        .toString()
    return "$tickerId,$shares,$purchasePrice,${purchaseCurrency.code},$date"
}
