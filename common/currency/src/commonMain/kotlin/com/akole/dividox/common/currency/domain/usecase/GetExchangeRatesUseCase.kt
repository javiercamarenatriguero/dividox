package com.akole.dividox.common.currency.domain.usecase

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates
import com.akole.dividox.common.currency.domain.repository.ExchangeRateRepository

/**
 * Retrieves exchange rates for a given base currency.
 *
 * Delegates to [ExchangeRateRepository], which handles caching and network access.
 *
 * @param repository The repository used to fetch or serve cached rates.
 */
class GetExchangeRatesUseCase(private val repository: ExchangeRateRepository) {

    /**
     * Fetches exchange rates with [base] as the reference currency.
     *
     * @param base Currency from which all rates are expressed.
     * @return [Result.success] wrapping [ExchangeRates], or [Result.failure] on error.
     */
    suspend operator fun invoke(base: Currency): Result<ExchangeRates> =
        repository.getExchangeRates(base)
}
