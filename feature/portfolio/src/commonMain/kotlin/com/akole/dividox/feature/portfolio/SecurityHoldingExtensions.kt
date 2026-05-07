package com.akole.dividox.feature.portfolio

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.integration.security.domain.model.SecurityHolding

internal fun List<SecurityHolding>.filterByQuery(query: String): List<SecurityHolding> {
    if (query.isBlank()) return this
    val lowerQuery = query.lowercase()
    return filter { holding ->
        holding.holding.tickerId.lowercase().contains(lowerQuery) ||
            holding.quote.name?.lowercase()?.contains(lowerQuery) == true
    }
}

internal fun List<SecurityHolding>.sortBy(sortOrder: SortOrder): List<SecurityHolding> {
    val comparator: Comparator<SecurityHolding> = when (sortOrder.field) {
        SortField.GAIN -> compareBy { it.totalGainPercent }
        SortField.VALUE -> compareBy { it.holding.shares * it.quote.price }
        SortField.DIVIDEND -> compareBy { it.dividendInfo?.yield ?: 0.0 }
        SortField.DATE -> compareBy { it.holding.purchaseDate }
    }
    return if (sortOrder.ascending) sortedWith(comparator) else sortedWith(comparator.reversed())
}

internal suspend fun CurrencyConverter.convertHoldingPrices(
    holdings: List<SecurityHolding>,
    to: Currency,
): Map<String, Double> = buildMap {
    holdings.forEach { holding ->
        val ticker = holding.holding.tickerId
        val price = holding.quote.price
        val from = Currency.entries.firstOrNull { it.code == holding.quote.currency } ?: Currency.USD
        val converted = convert(price, from, to).getOrElse { price }
        put(ticker, converted)
    }
}
