package com.akole.dividox.common.settings.domain.usecase

import com.akole.dividox.common.settings.data.biometric.BiometricAuthenticator
import com.akole.dividox.common.settings.data.biometric.BiometricResult
import com.akole.dividox.common.settings.domain.datastore.AppSettingsDataStore

class UpdateBiometricLockUseCase(
    private val dataStore: AppSettingsDataStore,
    private val authenticator: BiometricAuthenticator,
) {
    suspend operator fun invoke(enabled: Boolean): BiometricResult {
        if (enabled) {
            val result = authenticator.authenticate()
            if (result !is BiometricResult.Success) return result
        }
        dataStore.setBiometricLock(enabled)
        return BiometricResult.Success
    }
}
