package com.akole.dividox.common.ui.resources.format

fun Double.formatPrice(currencyCode: String): String =
    "${currencyCode.toCurrencySymbol()}${"%.2f".format(this)}"

fun Double.formatPercent(): String = "${"%.2f".format(this)}%"

fun Double.formatPercentSigned(): String {
    val sign = if (this >= 0.0) "+" else ""
    return "$sign${"%.2f".format(this)}%"
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
