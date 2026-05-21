package com.akole.dividox.common.settings.domain.model

import com.akole.dividox.common.currency.domain.model.Currency

data class AppSettings(
    val currency: Currency = Currency.EUR,
    val biometricLockEnabled: Boolean = false,
    val defaultMarket: String = "ALL",
    val onboardingCompleted: Boolean = false,
)
