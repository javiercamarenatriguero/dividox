package com.akole.dividox.common.currency.data.dto

import kotlinx.serialization.Serializable

/**
 * Compact local cache entry stored as JSON in DataStore Preferences.
 *
 * The base currency is omitted because it is encoded in the DataStore key
 * (e.g. key `"rates_EUR"` implies `base = EUR`), keeping the stored payload minimal.
 *
 * @property date  ISO 8601 date string of the rates snapshot (e.g. `"2025-01-15"`).
 * @property rates Map of ISO 4217 target currency codes to their rates relative to the base.
 */
@Serializable
internal data class CachedExchangeRatesDto(
    val date: String,
    val rates: Map<String, Double>,
)
