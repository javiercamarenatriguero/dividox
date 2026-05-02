package com.akole.dividox.common.currency.data.datasource

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates

/**
 * Local persistence contract for exchange rate data.
 *
 * Provides read/write access to rates previously fetched from the network,
 * enabling offline use and avoiding redundant API calls within the same day.
 */
interface LocalExchangeRateDataSource {

    /**
     * Returns persisted rates for [base], or `null` if no entry exists.
     *
     * @param base The base currency whose rates are requested.
     */
    suspend fun get(base: Currency): ExchangeRates?

    /**
     * Persists [rates] for future offline use, keyed by [ExchangeRates.base].
     *
     * @param rates The exchange rates to store.
     */
    suspend fun save(rates: ExchangeRates)
}
