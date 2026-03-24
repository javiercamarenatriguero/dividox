package com.akole.dividox.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.feature.home.HomeContract.HomeSideEffect
import com.akole.dividox.feature.home.HomeContract.HomeViewEvent
import com.akole.dividox.feature.home.HomeContract.HomeViewState

class HomeViewModel(
    greeting: String,
    private val platformName: String,
) : ViewModel(),
    MVI<HomeViewState, HomeViewEvent, HomeSideEffect> by mvi(HomeViewState()) {

    init {
        updateViewState { copy(greeting = greeting) }
    }

    override fun onViewEvent(viewEvent: HomeViewEvent) {
        when (viewEvent) {
            HomeViewEvent.OnButtonClicked -> onButtonClicked()
            HomeViewEvent.OnDetailClicked -> {
                viewModelScope.emitSideEffect(
                    HomeSideEffect.Navigation.NavigateToDetail(platformName),
                )
            }
        }
    }

    private fun onButtonClicked() {
        updateViewState { copy(showContent = !showContent) }
    }
}
