package com.akole.dividox.common.auth.domain.repository

import com.akole.dividox.common.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class StubAuthRepository : AuthRepository {
    override fun observeAuthState(): Flow<AuthUser?> = flowOf(null)
}
