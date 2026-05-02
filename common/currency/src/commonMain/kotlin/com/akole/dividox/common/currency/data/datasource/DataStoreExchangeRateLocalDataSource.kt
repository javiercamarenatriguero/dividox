package com.akole.dividox.common.currency.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.akole.dividox.common.currency.data.dto.CachedExchangeRatesDto
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persists exchange rates in DataStore Preferences as compact JSON.
 *
 * Storage layout — one key per base currency:
 * - Key: `"rates_EUR"`, `"rates_USD"`, etc. (derived from [Currency.code])
 * - Value: `{"date":"2025-01-15","rates":{"USD":1.05,"GBP":0.85}}`
 *
 * The base currency is not stored inside the JSON payload because it is already
 * encoded in the key, keeping the stored size minimal.
 *
 * @param dataStore The DataStore instance backed by `dividox_exchange_rates.preferences_pb`.
 */
internal class DataStoreExchangeRateLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) : LocalExchangeRateDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Reads the JSON entry for [base] from DataStore and deserialises it.
     *
     * Steps:
     * 1. Reads the Preferences snapshot from DataStore.
     * 2. Looks up the entry by key `"rates_{base.code}"`.
     * 3. Decodes the JSON string into [CachedExchangeRatesDto].
     * 4. Maps the DTO to [ExchangeRates], ignoring unknown currency codes.
     *
     * @param base The base currency whose cached rates are requested.
     * @return The cached [ExchangeRates], or `null` if no entry exists or decoding fails.
     */
    override suspend fun get(base: Currency): ExchangeRates? {
        val key = prefsKeyFor(base)
        val raw = dataStore.data.first()[key] ?: return null
        return try {
            val dto = json.decodeFromString<CachedExchangeRatesDto>(raw)
            dto.toDomain(base)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Serialises [rates] and writes them to DataStore under key `"rates_{base.code}"`.
     *
     * Steps:
     * 1. Converts [ExchangeRates.rates] entries to a `Map<String, Double>` (using [Currency.code] as key).
     * 2. Wraps the result in [CachedExchangeRatesDto] with the ISO date string.
     * 3. Encodes the DTO to a JSON string and stores it in Preferences.
     *
     * @param rates The exchange rates snapshot to persist.
     */
    override suspend fun save(rates: ExchangeRates) {
        val key = prefsKeyFor(rates.base)
        val dto = CachedExchangeRatesDto(
            date = rates.date.toString(),
            rates = rates.rates.entries.associate { (c, r) -> c.code to r },
        )
        dataStore.edit { prefs ->
            prefs[key] = json.encodeToString(dto)
        }
    }

    private fun CachedExchangeRatesDto.toDomain(base: Currency): ExchangeRates =
        ExchangeRates(
            base = base,
            date = LocalDate.parse(date),
            rates = rates.entries
                .mapNotNull { (code, rate) ->
                    Currency.entries.firstOrNull { it.code == code }?.let { it to rate }
                }
                .toMap(),
        )

    private companion object {
        fun prefsKeyFor(base: Currency) = stringPreferencesKey("rates_${base.code}")
    }
}
