package com.akole.dividox.common.currency.data.dto

import kotlinx.serialization.Serializable

/**
 * Frankfurter API response DTO.
 *
 * Example endpoint: `GET https://api.frankfurter.dev/v1/latest?base=EUR`
 *
 * @property amount The base amount (always `1.0` for the `/latest` endpoint).
 * @property base   ISO 4217 code of the base currency (e.g. `"EUR"`).
 * @property date   ISO 8601 date string when the rates were last updated (e.g. `"2025-01-15"`).
 * @property rates  Map of target currency codes to their exchange rates relative to [base].
 */
@Serializable
data class ExchangeRateResponseDto(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>,
)
