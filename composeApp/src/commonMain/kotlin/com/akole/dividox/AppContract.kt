package com.akole.dividox

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState

interface AppContract {

    data class AppViewState(
        val showContent: Boolean = false,
        val greeting: String = "",
    ) : ViewState

    sealed interface AppViewEvent : ViewEvent {
        data object OnButtonClicked : AppViewEvent
    }

    sealed interface AppSideEffect : SideEffect
}
