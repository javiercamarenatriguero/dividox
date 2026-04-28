package com.akole.dividox.component.auth.domain.model

sealed interface SessionState {
    data object Loading : SessionState
    data class Authenticated(val user: AuthUser) : SessionState
    data object Unauthenticated : SessionState
}
