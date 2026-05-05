package com.akole.dividox.feature.dashboard

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.integration.security.domain.model.EnrichedWatchlistEntry
import com.akole.dividox.integration.security.domain.model.PortfolioSummary

internal suspend fun CurrencyConverter.convertAmount(amount: Double, to: Currency): Double {
    if (to == Currency.USD) return amount
    return getRate(Currency.USD, to).getOrNull()?.let { rate -> amount * rate } ?: amount
}

internal suspend fun CurrencyConverter.convertSummary(summary: PortfolioSummary, to: Currency): PortfolioSummary {
    if (to == Currency.USD) return summary
    val rate = getRate(Currency.USD, to).getOrNull() ?: return summary
    return PortfolioSummary(
        totalValue = summary.totalValue * rate,
        totalGain = summary.totalGain * rate,
        totalGainPercent = summary.totalGainPercent,
        totalYield = summary.totalYield,
        dividendsCollected = summary.dividendsCollected * rate,
    )
}

internal suspend fun CurrencyConverter.convertWatchlistPrices(
    watchlist: List<EnrichedWatchlistEntry>,
    to: Currency,
): Map<String, Double> = buildMap {
    watchlist.forEach { entry ->
        val quote = entry.quote ?: return@forEach
        val ticker = entry.entry.tickerId
        val from = Currency.entries.firstOrNull { it.code == quote.currency } ?: Currency.USD
        val converted = convert(quote.price, from, to).getOrElse { quote.price }
        put(ticker, converted)
    }
}
