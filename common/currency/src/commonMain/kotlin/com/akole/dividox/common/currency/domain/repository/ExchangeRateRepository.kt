package com.akole.dividox.common.currency.domain.repository

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates

/**
 * Contract for retrieving foreign exchange rates.
 *
 * Implementations are responsible for caching: repeated calls for the same [base]
 * on the same calendar day must not trigger a new network request.
 */
interface ExchangeRateRepository {

    /**
     * Returns today's exchange rates using [base] as the reference currency.
     *
     * @param base The currency from which all rates are quoted (e.g. EUR → USD rate means 1 EUR = X USD).
     * @return [Result.success] with [ExchangeRates] on success, [Result.failure] if the
     *         network is unreachable and no cached data is available.
     */
    suspend fun getExchangeRates(base: Currency): Result<ExchangeRates>
}
