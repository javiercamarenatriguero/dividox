package com.akole.dividox.common.settings.data.biometric

expect class BiometricAuthenticator {
    suspend fun authenticate(): BiometricResult
}
