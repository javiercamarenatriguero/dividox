package com.akole.dividox.common.ui.resources.format

import com.akole.dividox.common.currency.domain.model.Currency
import kotlin.math.abs
import kotlin.math.roundToLong

// ─── Internal helpers ─────────────────────────────────────────────────────────

private sealed interface NumberLocale {
    /** $1,234.50 — symbol before, comma grouping, period decimal (USD, GBP, …) */
    data object UsLike : NumberLocale
    /** 1.234,50 € — symbol after with space, period grouping, comma decimal (EUR) */
    data object EuLike : NumberLocale
}

private fun Currency.numberLocale(): NumberLocale = when (this) {
    Currency.EUR -> NumberLocale.EuLike
    else -> NumberLocale.UsLike
}

private fun Double.intAndDecFormatted(groupSep: Char): Pair<String, String> {
    val factor = 100L
    val rounded = (abs(this) * factor).roundToLong()
    val intPart = rounded / factor
    val decPart = rounded % factor
    val intFormatted = intPart.toString()
        .reversed().chunked(3)
        .joinToString(groupSep.toString())
        .reversed()
    return intFormatted to decPart.toString().padStart(2, '0')
}

// ─── Public API ───────────────────────────────────────────────────────────────

/**
 * Formats this number as a currency string following each currency's conventional format:
 * - USD/GBP/etc: `$1,234.50`  (symbol before, comma thousands, period decimal)
 * - EUR:         `1.234,50 €` (symbol after with space, period thousands, comma decimal)
 */
fun Double.formatPrice(currency: Currency): String {
    val sign = if (this < 0.0) "-" else ""
    return when (currency.numberLocale()) {
        NumberLocale.UsLike -> {
            val (intF, dec) = formattedAbsIntAndDec(',')
            "$sign${currency.symbol}$intF.$dec"
        }
        NumberLocale.EuLike -> {
            val (intF, dec) = formattedAbsIntAndDec('.')
            "$sign$intF,$dec ${currency.symbol.trim()}"
        }
    }
}

/**
 * Formats using an ISO currency code string.
 * Delegates to [formatPrice] if the code matches a known [Currency], otherwise
 * falls back to a neutral `CODE 1,234.50` format.
 */
fun Double.formatPrice(currencyCode: String): String {
    val currency = Currency.entries.firstOrNull { it.code == currencyCode }
    return if (currency != null) {
        formatPrice(currency)
    } else {
        val sign = if (this < 0.0) "-" else ""
        val (intF, dec) = formattedAbsIntAndDec(',')
        "$sign$currencyCode $intF.$dec"
    }
}

/** Formats as a percentage string (locale-neutral, period decimal). */
fun Double.formatPercent(): String = "${formatTwoDecimals()}%"

/** Formats as a signed percentage string (e.g. `+2.34%` or `-1.20%`). */
fun Double.formatPercentSigned(): String {
    val sign = if (this >= 0.0) "+" else ""
    return "$sign${formatTwoDecimals()}%"
}

/**
 * Formats as a signed currency string with an explicit `+` prefix for non-negative values.
 * Follows the same locale rules as [formatPrice].
 */
fun Double.formatPriceSigned(currency: Currency): String {
    val sign = if (this >= 0.0) "+" else "-"
    val absValue = abs(this)
    return when (currency.numberLocale()) {
        NumberLocale.UsLike -> {
            val (intF, dec) = absValue.formattedAbsIntAndDec(',')
            "$sign${currency.symbol}$intF.$dec"
        }
        NumberLocale.EuLike -> {
            val (intF, dec) = absValue.formattedAbsIntAndDec('.')
            "$sign$intF,$dec ${currency.symbol.trim()}"
        }
    }
}

/**
 * Raw two-decimal formatter — locale-neutral, always uses period as decimal separator.
 * Use for percentages, ratios, or values where locale formatting is not needed.
 */
fun Double.formatTwoDecimals(): String {
    val factor = 100L
    val rounded = (abs(this) * factor).roundToLong()
    val intPart = abs(rounded / factor)
    val decPart = abs(rounded % factor)
    val sign = if (this < 0.0) "-" else ""
    return "$sign$intPart.${decPart.toString().padStart(2, '0')}"
}

private fun Double.formattedAbsIntAndDec(groupSep: Char): Pair<String, String> =
    abs(this).intAndDecFormatted(groupSep)

/**
 * Formats index points using EU-like notation: period thousands separator, comma decimal.
 * e.g. 38490.14 → "38.490,14"
 */
fun Double.formatIndexPoints(): String {
    val sign = if (this < 0.0) "-" else ""
    val (intF, dec) = abs(this).intAndDecFormatted('.')
    return "$sign$intF,$dec"
}

/** Signed variant of [formatIndexPoints], e.g. +382,00 or -54,10. */
fun Double.formatIndexPointsSigned(): String {
    val sign = if (this >= 0.0) "+" else "-"
    val (intF, dec) = abs(this).intAndDecFormatted('.')
    return "$sign$intF,$dec"
}

/** Signed EU-like percent for index cards: +2,16% or -1,08%. */
fun Double.formatIndexPercent(): String {
    val sign = if (this >= 0.0) "+" else "-"
    val factor = 100L
    val rounded = (abs(this) * factor).roundToLong()
    val intPart = rounded / factor
    val decPart = (rounded % factor).toString().padStart(2, '0')
    return "$sign$intPart,$decPart%"
}

/**
 * Formats a large number with a compact suffix: B (billions), M (millions), K (thousands).
 * Values below 1 000 are formatted with two decimal places and no suffix.
 *
 * Examples:
 * - `1_234_567_890.0` → `"1.23B"`
 * - `450_000_000.0` → `"450.00M"`
 * - `12_300.0` → `"12.30K"`
 * - `500.0` → `"500.00"`
 */
fun Double.formatLargeNumber(): String {
    val absVal = abs(this)
    val sign = if (this < 0.0) "-" else ""
    return when {
        absVal >= 1_000_000_000.0 -> "$sign${(absVal / 1_000_000_000.0).formatTwoDecimals()}B"
        absVal >= 1_000_000.0 -> "$sign${(absVal / 1_000_000.0).formatTwoDecimals()}M"
        absVal >= 1_000.0 -> "$sign${(absVal / 1_000.0).formatTwoDecimals()}K"
        else -> "$sign${absVal.formatTwoDecimals()}"
    }
}
