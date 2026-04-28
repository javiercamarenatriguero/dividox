package com.akole.dividox.feature.auth.login

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState

interface LoginContract {

    data class LoginViewState(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
    ) : ViewState

    sealed interface LoginViewEvent : ViewEvent {
        data class OnEmailChanged(val email: String) : LoginViewEvent
        data class OnPasswordChanged(val password: String) : LoginViewEvent
        data object OnSignInClicked : LoginViewEvent
        data object OnGoogleSignInClicked : LoginViewEvent
        data object OnForgotPasswordClicked : LoginViewEvent
        data object OnSignUpClicked : LoginViewEvent
        data object OnErrorDismissed : LoginViewEvent
    }

    sealed interface LoginSideEffect : SideEffect {
        sealed interface Navigation : LoginSideEffect {
            data object NavigateToSignUp : Navigation
            data class NavigateToForgotPassword(val email: String) : Navigation
        }
    }
}
