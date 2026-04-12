package com.akole.dividox.common.auth.domain.repository

import com.akole.dividox.common.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthUser?>
}
