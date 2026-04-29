package com.akole.dividox.common.settings.domain.model

import com.akole.dividox.common.ui.resources.Currency

data class AppSettings(
    val currency: Currency = Currency.EUR,
)
