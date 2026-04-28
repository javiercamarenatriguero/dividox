package com.akole.dividox.component.auth.data

actual open class GoogleSignInLauncher {
    actual open suspend fun launchSignIn(): String? {
        throw UnsupportedOperationException("Google Sign-In not available on Desktop")
    }
}
