package com.akole.dividox.component.auth.data

import com.akole.dividox.component.auth.domain.exception.RecentLoginRequiredException
import com.akole.dividox.component.auth.domain.model.AuthError
import com.akole.dividox.component.auth.domain.model.AuthUser
import com.akole.dividox.component.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of [AuthRepository] delegating to platform-specific [AuthDataSource].
 * Wraps all operations in [Result] for consistent error handling across platforms.
 */
class AuthRepositoryImpl(
    private val dataSource: AuthDataSource
) : AuthRepository {
    override fun observeAuthState(): Flow<AuthUser?> = dataSource.observeAuthState()

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = try {
        dataSource.signInWithEmail(email, password)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toAuthError())
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = try {
        dataSource.signUpWithEmail(email, password)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toAuthError())
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = try {
        dataSource.signInWithGoogle(idToken)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toAuthError())
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = try {
        dataSource.sendPasswordResetEmail(email)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toAuthError())
    }

    override suspend fun signOut(): Result<Unit> = try {
        dataSource.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toAuthError())
    }

    override fun getCurrentUserId(): String? = dataSource.getCurrentUserId()

    override suspend fun ensureTokenReady() = dataSource.ensureTokenReady()

    override suspend fun deleteAccount(): Result<Unit> = try {
        dataSource.deleteAccount()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toAuthError())
    }
}

internal fun Throwable.toAuthError(): AuthError {
    if (this is RecentLoginRequiredException) return AuthError.RecentLoginRequired
    val msg = message?.lowercase() ?: return AuthError.Unknown("Unknown authentication error")
    return when {
        msg.contains("weak password") ||
            msg.contains("at least 6 characters") -> AuthError.WeakPassword

        msg.contains("password is invalid") ||
            msg.contains("invalid credential") ||
            msg.contains("credential is incorrect") ||
            msg.contains("wrong password") ||
            msg.contains("wrong_password") ||
            msg.contains("invalid login") -> AuthError.InvalidCredentials

        msg.contains("no user record") ||
            msg.contains("user not found") ||
            msg.contains("no user") -> AuthError.AccountNotFound

        msg.contains("already in use") ||
            msg.contains("email address is already") -> AuthError.EmailAlreadyInUse

        msg.contains("badly formatted") ||
            msg.contains("invalid email") -> AuthError.InvalidEmail

        msg.contains("network") ||
            msg.contains("timeout") ||
            msg.contains("unable to resolve") -> AuthError.NetworkError

        msg.contains("too many") ||
            msg.contains("unusual activity") ||
            msg.contains("blocked") -> AuthError.TooManyAttempts

        msg.contains("recent login") ||
            msg.contains("reauthenticate") -> AuthError.RecentLoginRequired

        else -> AuthError.Unknown(message ?: "Unknown authentication error")
    }
}
