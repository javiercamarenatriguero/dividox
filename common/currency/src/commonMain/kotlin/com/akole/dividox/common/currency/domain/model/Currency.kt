package com.akole.dividox.common.currency.domain.model

/**
 * ISO 4217 currency codes supported by the app.
 *
 * The set is limited to currencies available in the Frankfurter API (ECB data).
 * Used across the domain, settings, and UI layers.
 *
 * @property code ISO 4217 three-letter currency code (e.g. "EUR", "USD").
 * @property symbol Human-readable currency symbol (e.g. "€", "$").
 */
enum class Currency(val code: String, val symbol: String) {
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£"),
    JPY("JPY", "¥"),
    CHF("CHF", "CHF "),
    CAD("CAD", "CA$"),
    AUD("AUD", "A$"),
    NZD("NZD", "NZ$"),
    CNY("CNY", "¥"),
    INR("INR", "₹"),
    MXN("MXN", "MX$"),
    BRL("BRL", "R$"),
    ZAR("ZAR", "R"),
}
