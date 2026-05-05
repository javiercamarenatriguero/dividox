package com.akole.dividox.feature.favorites

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.integration.security.domain.model.EnrichedWatchlistEntry

internal suspend fun CurrencyConverter.convertWatchlistPrices(
    entries: List<EnrichedWatchlistEntry>,
    to: Currency,
): Map<String, Double> = buildMap {
    entries.forEach { entry ->
        val quote = entry.quote ?: return@forEach
        val ticker = entry.entry.tickerId
        val from = Currency.entries.firstOrNull { it.code == quote.currency } ?: Currency.USD
        val converted = convert(quote.price, from, to).getOrElse { quote.price }
        put(ticker, converted)
    }
}
