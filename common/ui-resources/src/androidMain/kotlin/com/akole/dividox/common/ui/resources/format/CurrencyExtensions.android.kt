package com.akole.dividox.common.ui.resources.format

import com.akole.dividox.common.currency.domain.model.Currency

actual fun Currency.flag(): String = when (this) {
    Currency.USD -> "🇺🇸"
    Currency.EUR -> "🇪🇺"
    Currency.GBP -> "🇬🇧"
    Currency.GBX -> "🇬🇧"
    Currency.JPY -> "🇯🇵"
    Currency.CHF -> "🇨🇭"
    Currency.CAD -> "🇨🇦"
    Currency.AUD -> "🇦🇺"
    Currency.NZD -> "🇳🇿"
    Currency.CNY -> "🇨🇳"
    Currency.INR -> "🇮🇳"
    Currency.MXN -> "🇲🇽"
    Currency.BRL -> "🇧🇷"
    Currency.ZAR -> "🇿🇦"
}
