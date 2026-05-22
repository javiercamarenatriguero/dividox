package com.akole.dividox.feature.onboarding

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState

data class OnboardingState(
    val currentPage: Int = 0,
    val totalPages: Int = 5,
) : ViewState

sealed interface OnboardingEvent : ViewEvent {
    data object OnNextClicked : OnboardingEvent
    data object OnSkipClicked : OnboardingEvent
    data class OnPageChanged(val page: Int) : OnboardingEvent
}

sealed interface OnboardingSideEffect : SideEffect {
    data object NavigateToDashboard : OnboardingSideEffect
}
