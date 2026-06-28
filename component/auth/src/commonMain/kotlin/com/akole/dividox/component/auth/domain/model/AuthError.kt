package com.akole.dividox.component.auth.domain.model

sealed class AuthError : Exception() {
    data object InvalidCredentials : AuthError()
    data object AccountNotFound : AuthError()
    data object EmailAlreadyInUse : AuthError()
    data object WeakPassword : AuthError()
    data object InvalidEmail : AuthError()
    data object NetworkError : AuthError()
    data object TooManyAttempts : AuthError()
    data object RecentLoginRequired : AuthError()
    data object TermsNotAccepted : AuthError()
    data class Unknown(override val message: String) : AuthError()
}
