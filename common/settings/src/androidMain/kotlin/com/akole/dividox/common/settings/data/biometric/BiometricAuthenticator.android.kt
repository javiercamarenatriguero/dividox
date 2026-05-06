package com.akole.dividox.common.settings.data.biometric

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class BiometricAuthenticator {
    actual fun canAuthenticate(): Boolean {
        return ActivityHolder.get() != null
    }

    actual suspend fun authenticate(): BiometricResult {
        val activity = ActivityHolder.get()
            ?: return BiometricResult.NotAvailable

        return suspendCancellableCoroutine { cont ->
            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (cont.isActive) cont.resume(BiometricResult.Success)
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (cont.isActive) cont.resume(BiometricResult.Failure(errString.toString()))
                }
                // onAuthenticationFailed: user can retry, don't resume
            }
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock DiviDox")
                .setNegativeButtonText("Cancel")
                .build()
            BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
        }
    }
}
