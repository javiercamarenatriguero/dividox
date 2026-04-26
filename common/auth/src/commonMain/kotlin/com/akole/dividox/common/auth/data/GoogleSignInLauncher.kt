package com.akole.dividox.common.auth.data

/**
 * Platform-specific Google Sign-In launcher using native SDKs.
 * - Android: Credential Manager API
 * - iOS: GoogleSignIn-iOS interop
 * - Desktop: Unsupported (throws exception)
 */
expect class GoogleSignInLauncher {
    /**
     * Launches Google Sign-In flow and returns ID token.
     * @return ID token on success, null if user cancelled
     * @throws UnsupportedOperationException on Desktop
     */
    suspend fun launchSignIn(): String?
}
