package com.akole.dividox.feature.auth.forgotpassword

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState

interface ForgotPasswordContract {

    data class ForgotPasswordViewState(
        val email: String = "",
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val error: String? = null,
    ) : ViewState

    sealed interface ForgotPasswordViewEvent : ViewEvent {
        data class OnEmailChanged(val email: String) : ForgotPasswordViewEvent
        data object OnSendResetLinkClicked : ForgotPasswordViewEvent
        data object OnBackClicked : ForgotPasswordViewEvent
    }

    sealed interface ForgotPasswordSideEffect : SideEffect {
        sealed interface Navigation : ForgotPasswordSideEffect {
            data object NavigateBack : Navigation
        }
    }
}
