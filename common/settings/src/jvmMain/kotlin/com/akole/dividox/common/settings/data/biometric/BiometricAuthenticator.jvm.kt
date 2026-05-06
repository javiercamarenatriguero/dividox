package com.akole.dividox.common.settings.data.biometric

actual class BiometricAuthenticator {
    actual suspend fun authenticate(): BiometricResult = BiometricResult.NotAvailable
}
