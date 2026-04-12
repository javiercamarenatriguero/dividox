package com.akole.dividox.common.auth.domain.model

sealed interface SessionState {
    data object Loading : SessionState
    data class Authenticated(val user: AuthUser) : SessionState
    data object Unauthenticated : SessionState
}
