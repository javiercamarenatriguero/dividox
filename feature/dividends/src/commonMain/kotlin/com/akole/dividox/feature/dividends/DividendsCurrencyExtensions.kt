package com.akole.dividox.feature.dividends

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.integration.dividend.domain.model.DividendActivitySummary
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import com.akole.dividox.integration.dividend.domain.model.MonthBar

internal suspend fun CurrencyConverter.convertBarAmounts(bars: List<MonthBar>, to: Currency): List<MonthBar> {
    if (to == Currency.USD) return bars
    val rate = getRate(Currency.USD, to).getOrNull() ?: return bars
    return bars.map { bar -> bar.copy(amount = bar.amount * rate) }
}

internal suspend fun CurrencyConverter.convertSummaryAmounts(
    summary: DividendActivitySummary?,
    to: Currency,
): DividendActivitySummary? {
    if (summary == null || to == Currency.USD) return summary
    val rate = getRate(Currency.USD, to).getOrNull() ?: return summary
    return summary.copy(
        lifetime = summary.lifetime * rate,
        ytd = summary.ytd * rate,
    )
}

internal suspend fun CurrencyConverter.convertPaymentAmounts(
    payments: List<EnrichedPayment>,
    to: Currency,
): List<EnrichedPayment> {
    if (to == Currency.USD) return payments
    val sourceCurrencies = payments.mapNotNull { enriched ->
        Currency.entries.firstOrNull { it.code == enriched.payment.currency }
    }.toSet()
    val rateMap: Map<Currency, Double> = sourceCurrencies
        .filter { it != to }
        .associateWith { from -> getRate(from, to).getOrNull() ?: 1.0 }
    return payments.map { enriched ->
        val from = Currency.entries.firstOrNull { it.code == enriched.payment.currency } ?: Currency.USD
        val rate = rateMap[from] ?: return@map enriched
        enriched.copy(payment = enriched.payment.copy(amount = enriched.payment.amount * rate))
    }
}
