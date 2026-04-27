package com.akole.dividox.common.auth.data

import com.akole.dividox.common.auth.domain.model.AuthUser

/**
 * Platform-specific authentication data source for Firebase and native SDKs.
 * - Android: delegates to Firebase Authentication SDK
 * - iOS: delegates to Firebase Authentication via Kotlin/Native interop
 * - Desktop: Firebase REST with proactive token refresh (TK-012)
 */
expect class AuthDataSource() {
    /**
     * Signs in user with Firebase email/password authentication.
     * @param email User email
     * @param password User password
     * @return Authenticated [AuthUser]
     * @throws Exception on authentication failure
     */
    suspend fun signInWithEmail(email: String, password: String): AuthUser

    /**
     * Creates new user in Firebase with email and password.
     * @param email User email
     * @param password User password
     * @return New authenticated [AuthUser]
     * @throws Exception on creation failure
     */
    suspend fun signUpWithEmail(email: String, password: String): AuthUser

    /**
     * Exchanges Google ID token for Firebase authentication.
     * @param idToken Google ID token from sign-in flow
     * @return Authenticated [AuthUser]
     * @throws Exception on authentication failure
     */
    suspend fun signInWithGoogle(idToken: String): AuthUser

    /**
     * Sends password reset email via Firebase.
     * @param email User email address
     * @throws Exception on send failure
     */
    suspend fun sendPasswordResetEmail(email: String)

    /**
     * Signs out current user from Firebase.
     */
    suspend fun signOut()

    /**
     * Observes current authentication state from Firebase.
     * @return Flow emitting current user or null when unauthenticated
     */
    fun observeAuthState(): kotlinx.coroutines.flow.Flow<AuthUser?>
}
