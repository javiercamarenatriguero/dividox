package com.akole.dividox.common.auth.data

import com.akole.dividox.common.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class AuthDataSource {
    actual suspend fun signInWithEmail(email: String, password: String): AuthUser {
        throw UnsupportedOperationException("Desktop auth not implemented")
    }

    actual suspend fun signUpWithEmail(email: String, password: String): AuthUser {
        throw UnsupportedOperationException("Desktop auth not implemented")
    }

    actual suspend fun signInWithGoogle(idToken: String): AuthUser {
        throw UnsupportedOperationException("Desktop auth not implemented")
    }

    actual suspend fun sendPasswordResetEmail(email: String) {
        throw UnsupportedOperationException("Desktop auth not implemented")
    }

    actual suspend fun signOut() {
        throw UnsupportedOperationException("Desktop auth not implemented")
    }

    actual fun observeAuthState(): Flow<AuthUser?> = flowOf(null)
}
