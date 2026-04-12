package com.akole.dividox.common.auth.domain.model

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val provider: AuthProvider,
)
