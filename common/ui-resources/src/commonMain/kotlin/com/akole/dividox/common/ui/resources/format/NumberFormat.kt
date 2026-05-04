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
