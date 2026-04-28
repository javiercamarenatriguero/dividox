package com.akole.dividox.component.auth.data

import com.akole.dividox.component.auth.domain.model.AuthUser

/**
 * Internal contract for persisting and retrieving authenticated user sessions.
 * Platform-specific implementations handle secure storage (encrypted file on Desktop, SDK on Mobile).
 */
interface SessionStorage {
    /**
     * Persists authenticated user session.
     * @param user Authenticated user to store
     */
    suspend fun saveSession(user: AuthUser)

    /**
     * Retrieves cached user session.
     * @return Stored [AuthUser] or null if no active session
     */
    suspend fun loadSession(): AuthUser?

    /**
     * Clears persisted session data.
     */
    suspend fun clearSession()
}
