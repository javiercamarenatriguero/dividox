package com.akole.dividox.common.auth.data

actual class GoogleSignInLauncher {
    actual suspend fun launchSignIn(): String? {
        throw UnsupportedOperationException("Google Sign-In launcher not implemented for iOS")
    }
}
