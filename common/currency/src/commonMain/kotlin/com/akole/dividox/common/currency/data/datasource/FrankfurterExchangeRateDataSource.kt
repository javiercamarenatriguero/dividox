package com.akole.dividox.common.currency.data.datasource

import com.akole.dividox.common.currency.data.dto.ExchangeRateResponseDto
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate

/**
 * Fetches live exchange rates from the Frankfurter public API.
 *
 * Frankfurter is open-source and powered by ECB data.
 * No API key required. Rates update daily around 16:00 CET.
 *
 * Endpoint: `GET https://api.frankfurter.dev/v1/latest?base={code}`
 *
 * @param httpClient Configured [HttpClient] with [ContentNegotiation] + JSON installed.
 */
internal class FrankfurterExchangeRateDataSource(
    private val httpClient: HttpClient,
) : ExchangeRateDataSource {

    /**
     * Calls the Frankfurter `/latest` endpoint for the given [base] currency.
     *
     * Steps:
     * 1. Sends `GET /v1/latest?base={base.code}`.
     * 2. Deserialises the JSON body into [ExchangeRateResponseDto].
     * 3. Maps the DTO to the [ExchangeRates] domain model, dropping unknown currency codes.
     *
     * @param base The reference currency for the rate table.
     * @return [Result.success] with the mapped [ExchangeRates], or [Result.failure] wrapping
     *         any [Exception] thrown by the HTTP client or JSON deserialisation.
     */
    override suspend fun getExchangeRates(base: Currency): Result<ExchangeRates> = try {
        val dto: ExchangeRateResponseDto = httpClient
            .get("$BASE_URL/latest") {
                parameter("base", base.code)
            }
            .body()
        Result.success(dto.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun ExchangeRateResponseDto.toDomain(): ExchangeRates {
        val baseCurrency = Currency.entries.firstOrNull { it.code == base }
            ?: error("Unknown base currency code: $base")
        // Map raw string keys to Currency enum entries, silently dropping unknown codes
        val ratesMap = rates.entries
            .mapNotNull { (code, rate) ->
                Currency.entries.firstOrNull { it.code == code }?.let { currency -> currency to rate }
            }
            .toMap()
        return ExchangeRates(
            base = baseCurrency,
            date = LocalDate.parse(date),
            rates = ratesMap,
        )
    }

    private companion object {
        const val BASE_URL = "https://api.frankfurter.dev/v1"
    }
}
