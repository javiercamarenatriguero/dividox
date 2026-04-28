package com.akole.dividox.component.portfolio.domain.model

/**
 * ISO 4217 currency codes. Enum-based for type safety and common currency support.
 * Extend with additional currencies as needed.
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

