package com.akole.dividox.common.currency.domain.model

import kotlinx.datetime.LocalDate

/**
 * Exchange rates for a given base currency on a specific date.
 *
 * @property base The currency rates are quoted against.
 * @property date Date of the exchange rates.
 * @property rates Map of target currencies to their rates (1 [base] = rate [target]).
 */
data class ExchangeRates(
    val base: Currency,
    val date: LocalDate,
    val rates: Map<Currency, Double>,
)
