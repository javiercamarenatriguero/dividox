package com.akole.dividox.component.auth.domain.model

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val provider: AuthProvider,
)
