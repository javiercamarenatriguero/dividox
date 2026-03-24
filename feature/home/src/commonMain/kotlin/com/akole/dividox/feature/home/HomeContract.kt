package com.akole.dividox.feature.home

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState

interface HomeContract {

    data class HomeViewState(
        val showContent: Boolean = false,
        val greeting: String = "",
    ) : ViewState

    sealed interface HomeViewEvent : ViewEvent {
        data object OnButtonClicked : HomeViewEvent
        data object OnDetailClicked : HomeViewEvent
    }

    sealed interface HomeSideEffect : SideEffect {
        sealed interface Navigation : HomeSideEffect {
            data class NavigateToDetail(val platformName: String) : Navigation
        }
    }
}
