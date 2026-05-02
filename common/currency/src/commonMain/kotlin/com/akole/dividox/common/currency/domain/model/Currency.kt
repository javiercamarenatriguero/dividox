package com.akole.dividox.common.currency.domain.model

/**
 * ISO 4217 currency codes supported by the app.
 *
 * The set is limited to currencies available in the Frankfurter API (ECB data).
 * Used across the domain, settings, and UI layers.
 *
 * @property code ISO 4217 three-letter currency code (e.g. "EUR", "USD").
 */
enum class Currency(val code: String) {
    USD("USD"),
    EUR("EUR"),
    GBP("GBP"),
    JPY("JPY"),
    CHF("CHF"),
    CAD("CAD"),
    AUD("AUD"),
    NZD("NZD"),
    CNY("CNY"),
    INR("INR"),
    MXN("MXN"),
    BRL("BRL"),
    ZAR("ZAR"),
}
