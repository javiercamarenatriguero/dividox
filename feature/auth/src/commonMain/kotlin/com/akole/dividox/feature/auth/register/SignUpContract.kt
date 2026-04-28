package com.akole.dividox.feature.auth.register

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState

interface SignUpContract {

    data class SignUpViewState(
        val name: String = "",
        val email: String = "",
        val password: String = "",
        val termsAccepted: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
    ) : ViewState

    sealed interface SignUpViewEvent : ViewEvent {
        data class OnNameChanged(val name: String) : SignUpViewEvent
        data class OnEmailChanged(val email: String) : SignUpViewEvent
        data class OnPasswordChanged(val password: String) : SignUpViewEvent
        data class OnTermsChanged(val accepted: Boolean) : SignUpViewEvent
        data object OnCreateAccountClicked : SignUpViewEvent
        data object OnSignInClicked : SignUpViewEvent
        data object OnErrorDismissed : SignUpViewEvent
    }

    sealed interface SignUpSideEffect : SideEffect {
        sealed interface Navigation : SignUpSideEffect {
            data object NavigateToLogin : Navigation
        }
    }
}
