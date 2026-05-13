package com.akole.dividox.common.ui.resources.format

import com.akole.dividox.common.currency.domain.model.Currency

// Flag emojis don't render correctly in Skia (CMP iOS renderer).
// Return ISO 3166-1 alpha-2 country codes as a readable fallback.
actual fun Currency.flag(): String = when (this) {
    Currency.USD -> "US"
    Currency.EUR -> "EU"
    Currency.GBP -> "GB"
    Currency.GBX -> "GB"
    Currency.JPY -> "JP"
    Currency.CHF -> "CH"
    Currency.CAD -> "CA"
    Currency.AUD -> "AU"
    Currency.NZD -> "NZ"
    Currency.CNY -> "CN"
    Currency.INR -> "IN"
    Currency.MXN -> "MX"
    Currency.BRL -> "BR"
    Currency.ZAR -> "ZA"
}
