package com.akole.dividox.component.auth.domain.repository

import com.akole.dividox.component.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository for authentication operations. Manages user sign-in, sign-up, password reset, and session state.
 */
interface AuthRepository {
    /**
     * Observes current authentication state.
     * @return Flow emitting current [AuthUser] or null if unauthenticated
     */
    fun observeAuthState(): Flow<AuthUser?>

    /**
     * Signs in user with email and password.
     * @param email User email
     * @param password User password
     * @return Success with Unit or failure
     */
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

    /**
     * Creates new user with email and password.
     * @param email User email
     * @param password User password
     * @return Success with Unit or failure
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>

    /**
     * Signs in user with Google authentication.
     * @param idToken Google ID token from sign-in flow
     * @return Success with Unit or failure
     */
    suspend fun signInWithGoogle(idToken: String): Result<Unit>

    /**
     * Sends password reset email to user.
     * @param email User email address
     * @return Success with Unit or failure
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Signs out current user.
     * @return Success with Unit or failure
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Returns the UID of the currently authenticated user, or null if unauthenticated.
     * @return UID string or null
     */
    fun getCurrentUserId(): String?
}
