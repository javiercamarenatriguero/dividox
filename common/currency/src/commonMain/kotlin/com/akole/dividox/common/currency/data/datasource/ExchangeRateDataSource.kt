package com.akole.dividox.common.currency.data.datasource

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates

/**
 * Remote data source contract for fetching exchange rates from an external provider.
 */
interface ExchangeRateDataSource {

    /**
     * Fetches live exchange rates from the remote provider using [base] as the reference currency.
     *
     * @param base The base currency (e.g. [Currency.EUR]).
     * @return [Result.success] with [ExchangeRates], or [Result.failure] on network/parse error.
     */
    suspend fun getExchangeRates(base: Currency): Result<ExchangeRates>
}
