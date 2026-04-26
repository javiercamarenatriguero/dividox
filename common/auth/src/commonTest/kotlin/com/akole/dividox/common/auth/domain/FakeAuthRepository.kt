package com.akole.dividox.common.auth.domain

import com.akole.dividox.common.auth.domain.model.AuthProvider
import com.akole.dividox.common.auth.domain.model.AuthUser
import com.akole.dividox.common.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake implementation of [AuthRepository] for testing success scenarios.
 */
internal class FakeAuthRepository(private val user: AuthUser? = null) : AuthRepository {
    var signInWithEmailCalled = false
    var signUpWithEmailCalled = false
    var signInWithGoogleCalled = false
    var sendPasswordResetEmailCalled = false
    var signOutCalled = false

    override fun observeAuthState(): Flow<AuthUser?> = flowOf(user)

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        signInWithEmailCalled = true
        return Result.success(Unit)
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> {
        signUpWithEmailCalled = true
        return Result.success(Unit)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        signInWithGoogleCalled = true
        return Result.success(Unit)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        sendPasswordResetEmailCalled = true
        return Result.success(Unit)
    }

    override suspend fun signOut(): Result<Unit> {
        signOutCalled = true
        return Result.success(Unit)
    }
}

/**
 * Failing implementation of [AuthRepository] for testing error scenarios.
 */
internal class FailingAuthRepository : AuthRepository {
    override fun observeAuthState(): Flow<AuthUser?> = flowOf(null)

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        Result.failure(IllegalStateException("Sign in failed"))

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> =
        Result.failure(IllegalStateException("Sign up failed"))

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> =
        Result.failure(IllegalStateException("Google sign in failed"))

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        Result.failure(IllegalStateException("Password reset failed"))

    override suspend fun signOut(): Result<Unit> =
        Result.failure(IllegalStateException("Sign out failed"))
}
