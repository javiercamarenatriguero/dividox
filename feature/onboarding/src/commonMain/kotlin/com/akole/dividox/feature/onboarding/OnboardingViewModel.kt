package com.akole.dividox.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.settings.domain.usecase.SetOnboardingCompletedUseCase
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val setOnboardingCompleted: SetOnboardingCompletedUseCase,
) : ViewModel(),
    MVI<OnboardingState, OnboardingEvent, OnboardingSideEffect> by mvi(OnboardingState()) {

    override fun onViewEvent(viewEvent: OnboardingEvent) {
        when (viewEvent) {
            OnboardingEvent.OnNextClicked -> onNextClicked()
            OnboardingEvent.OnSkipClicked -> completeOnboarding()
            is OnboardingEvent.OnPageChanged -> updateViewState { copy(currentPage = viewEvent.page) }
        }
    }

    private fun onNextClicked() {
        val state = viewState.value
        if (state.currentPage < state.totalPages - 1) {
            updateViewState { copy(currentPage = currentPage + 1) }
        } else {
            completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            setOnboardingCompleted()
            viewModelScope.emitSideEffect(OnboardingSideEffect.NavigateToDashboard)
        }
    }
}
