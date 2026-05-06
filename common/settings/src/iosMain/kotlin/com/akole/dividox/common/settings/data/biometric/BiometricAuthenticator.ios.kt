package com.akole.dividox.common.settings.data.biometric

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class BiometricAuthenticator {
    actual suspend fun authenticate(): BiometricResult = suspendCancellableCoroutine { cont ->
        val context = LAContext()
        val canEvaluate = context.canEvaluatePolicy(
            policy = LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = null,
        )
        if (!canEvaluate) {
            cont.resume(BiometricResult.NotAvailable)
            return@suspendCancellableCoroutine
        }
        context.evaluatePolicy(
            policy = LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = "Unlock DiviDox",
        ) { success, error ->
            if (cont.isActive) {
                cont.resume(
                    if (success) BiometricResult.Success
                    else BiometricResult.Failure(error?.localizedDescription ?: "Authentication failed"),
                )
            }
        }
    }
}
