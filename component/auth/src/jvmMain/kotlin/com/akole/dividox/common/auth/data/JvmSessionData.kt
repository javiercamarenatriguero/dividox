package com.akole.dividox.component.auth.data

import com.akole.dividox.component.auth.domain.model.AuthProvider
import com.akole.dividox.component.auth.domain.model.AuthUser

/**
 * Internal token-aware session representation for Desktop (jvmMain only).
 * Includes idToken and refreshToken needed for proactive token refresh.
 * Not exposed to the domain layer — [AuthUser] stays clean.
 */
internal data class JvmSessionData(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val provider: AuthProvider,
    val idToken: String,
    val refreshToken: String,
) {
    fun toAuthUser() = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        provider = provider,
    )

    companion object {
        fun fromAuthUser(user: AuthUser, idToken: String, refreshToken: String) = JvmSessionData(
            uid = user.uid,
            email = user.email,
            displayName = user.displayName,
            provider = user.provider,
            idToken = idToken,
            refreshToken = refreshToken,
        )
    }
}
