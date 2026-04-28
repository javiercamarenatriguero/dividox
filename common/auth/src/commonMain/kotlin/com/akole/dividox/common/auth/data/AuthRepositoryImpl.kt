package com.akole.dividox.common.auth.data

import com.akole.dividox.common.auth.domain.model.AuthUser
import com.akole.dividox.common.auth.domain.repository.AuthRepository
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
        Result.failure(e)
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = try {
        dataSource.signUpWithEmail(email, password)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = try {
        dataSource.signInWithGoogle(idToken)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = try {
        dataSource.sendPasswordResetEmail(email)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signOut(): Result<Unit> = try {
        dataSource.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getCurrentUserId(): String? = dataSource.getCurrentUserId()
}
