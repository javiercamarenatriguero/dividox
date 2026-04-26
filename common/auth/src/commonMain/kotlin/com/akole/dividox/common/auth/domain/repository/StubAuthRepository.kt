package com.akole.dividox.common.auth.domain.repository

import com.akole.dividox.common.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class StubAuthRepository : AuthRepository {
    override fun observeAuthState(): Flow<AuthUser?> = flowOf(null)
    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = Result.failure(NotImplementedError())
    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = Result.failure(NotImplementedError())
    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = Result.failure(NotImplementedError())
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.failure(NotImplementedError())
    override suspend fun signOut(): Result<Unit> = Result.failure(NotImplementedError())
}
