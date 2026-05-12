package com.akole.dividox.common.ui.resources.format

import com.akole.dividox.common.currency.domain.model.Currency
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.currency_name_aud
import dividox.common.ui_resources.generated.resources.currency_name_brl
import dividox.common.ui_resources.generated.resources.currency_name_cad
import dividox.common.ui_resources.generated.resources.currency_name_chf
import dividox.common.ui_resources.generated.resources.currency_name_cny
import dividox.common.ui_resources.generated.resources.currency_name_eur
import dividox.common.ui_resources.generated.resources.currency_name_gbp
import dividox.common.ui_resources.generated.resources.currency_name_gbx
import dividox.common.ui_resources.generated.resources.currency_name_inr
import dividox.common.ui_resources.generated.resources.currency_name_jpy
import dividox.common.ui_resources.generated.resources.currency_name_mxn
import dividox.common.ui_resources.generated.resources.currency_name_nzd
import dividox.common.ui_resources.generated.resources.currency_name_usd
import dividox.common.ui_resources.generated.resources.currency_name_zar
import org.jetbrains.compose.resources.StringResource

fun Currency.flag(): String = when (this) {
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

fun Currency.nameRes(): StringResource = when (this) {
    Currency.USD -> Res.string.currency_name_usd
    Currency.EUR -> Res.string.currency_name_eur
    Currency.GBP -> Res.string.currency_name_gbp
    Currency.GBX -> Res.string.currency_name_gbx
    Currency.JPY -> Res.string.currency_name_jpy
    Currency.CHF -> Res.string.currency_name_chf
    Currency.CAD -> Res.string.currency_name_cad
    Currency.AUD -> Res.string.currency_name_aud
    Currency.NZD -> Res.string.currency_name_nzd
    Currency.CNY -> Res.string.currency_name_cny
    Currency.INR -> Res.string.currency_name_inr
    Currency.MXN -> Res.string.currency_name_mxn
    Currency.BRL -> Res.string.currency_name_brl
    Currency.ZAR -> Res.string.currency_name_zar
}
