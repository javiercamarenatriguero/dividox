package com.akole.dividox.common.ui.resources.format

import com.akole.dividox.common.ui.resources.Currency
import kotlin.math.abs
import kotlin.math.roundToLong

fun Double.formatPrice(currency: Currency): String = formatPrice(currency.code)

fun Double.formatPrice(currencyCode: String): String =
    "${currencyCode.toCurrencySymbol()}${formatTwoDecimals()}"

fun Double.formatPercent(): String = "${formatTwoDecimals()}%"

fun Double.formatPercentSigned(): String {
    val sign = if (this >= 0.0) "+" else ""
    return "$sign${formatTwoDecimals()}%"
}

private fun Double.formatTwoDecimals(): String {
    val factor = 100L
    val rounded = (this * factor).roundToLong()
    val intPart = abs(rounded / factor)
    val decPart = abs(rounded % factor)
    val sign = if (this < 0.0) "-" else ""
    return "$sign$intPart.${decPart.toString().padStart(2, '0')}"
}

private fun String.toCurrencySymbol(): String = when (this) {
    "USD" -> "$"
    "EUR" -> "€"
    "GBP" -> "£"
    "JPY" -> "¥"
    "CHF" -> "CHF "
    "CAD" -> "CA$"
    "AUD" -> "A$"
    "NZD" -> "NZ$"
    "CNY" -> "¥"
    "INR" -> "₹"
    "MXN" -> "MX$"
    "BRL" -> "R$"
    "ZAR" -> "R"
    else -> "$this "
}
