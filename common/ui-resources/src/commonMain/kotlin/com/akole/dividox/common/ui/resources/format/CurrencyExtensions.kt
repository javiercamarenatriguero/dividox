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
import dividox.common.ui_resources.generated.resources.currency_name_sek
import dividox.common.ui_resources.generated.resources.currency_name_nok
import dividox.common.ui_resources.generated.resources.currency_name_dkk
import dividox.common.ui_resources.generated.resources.currency_name_pln
import dividox.common.ui_resources.generated.resources.currency_name_hkd
import dividox.common.ui_resources.generated.resources.currency_name_sgd
import dividox.common.ui_resources.generated.resources.currency_name_krw
import org.jetbrains.compose.resources.StringResource

expect fun Currency.flag(): String

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
    Currency.SEK -> Res.string.currency_name_sek
    Currency.NOK -> Res.string.currency_name_nok
    Currency.DKK -> Res.string.currency_name_dkk
    Currency.PLN -> Res.string.currency_name_pln
    Currency.HKD -> Res.string.currency_name_hkd
    Currency.SGD -> Res.string.currency_name_sgd
    Currency.KRW -> Res.string.currency_name_krw
}
