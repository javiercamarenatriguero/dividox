package com.akole.dividox.common.currency

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.usecase.GetExchangeRatesUseCase

/**
 * Converts monetary amounts between currencies using live ECB exchange rates.
 *
 * Exchange rates are fetched from Frankfurter (https://api.frankfurter.dev) and cached
 * per base currency for the current calendar day.
 *
 * Intended to be used as a singleton via DI.
 */
class CurrencyConverter(
    private val getExchangeRates: GetExchangeRatesUseCase,
) {
    /**
     * Converts [amount] from [from] to [to] currency.
     *
     * Steps:
     * 1. Returns [amount] unchanged if [from] == [to] (no conversion needed).
     * 2. Fetches today's rates using [from] as base (served from cache when available).
     * 3. Looks up the rate for [to] in the rate table.
     * 4. Multiplies [amount] by the rate.
     *
     * @param amount The monetary value to convert.
     * @param from   The source currency.
     * @param to     The target currency.
     * @return [Result.success] with the converted amount, or [Result.failure] if rates
     *         could not be fetched or [to] is not present in the rate table.
     */
    suspend fun convert(amount: Double, from: Currency, to: Currency): Result<Double> {
        if (from == to) return Result.success(amount)
        return getExchangeRates(from).mapCatching { rates ->
            rates.rates[to] ?: error("No exchange rate found for ${to.code} (base: ${from.code})")
        }.map { rate -> amount * rate }
    }
}
