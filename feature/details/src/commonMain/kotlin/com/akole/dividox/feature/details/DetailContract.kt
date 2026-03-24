package com.akole.dividox.feature.details

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState

interface DetailContract {

    data class DetailViewState(
        val platformName: String = "",
        val greeting: String = "",
    ) : ViewState

    sealed interface DetailViewEvent : ViewEvent {
        data object OnBackClicked : DetailViewEvent
    }

    sealed interface DetailSideEffect : SideEffect {
        sealed interface Navigation : DetailSideEffect {
            data object NavigateBack : Navigation
        }
    }
}
