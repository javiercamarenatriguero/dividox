package com.akole.dividox.component.auth.domain.usecase

import com.akole.dividox.component.auth.domain.model.SessionState
import com.akole.dividox.component.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class ObserveSessionUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<SessionState> =
        repository.observeAuthState()
            .map { user ->
                if (user != null) SessionState.Authenticated(user)
                else SessionState.Unauthenticated
            }
            .onStart { emit(SessionState.Loading) }
}
