package com.akole.dividox.common.settings.data.biometric

sealed class BiometricResult {
    data object Success : BiometricResult()
    data class Failure(val reason: String) : BiometricResult()
    data object NotAvailable : BiometricResult()
}
