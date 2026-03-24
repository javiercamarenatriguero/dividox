package com.akole.dividox.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.Greeting
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.detail.DetailContract.DetailSideEffect
import com.akole.dividox.detail.DetailContract.DetailViewEvent
import com.akole.dividox.detail.DetailContract.DetailViewState

class DetailViewModel(
    platformName: String,
) : ViewModel(),
    MVI<DetailViewState, DetailViewEvent, DetailSideEffect>
    by mvi(DetailViewState()) {

    init {
        updateViewState {
            copy(
                platformName = platformName,
                greeting = Greeting().greet(),
            )
        }
    }

    override fun onViewEvent(viewEvent: DetailViewEvent) {
        when (viewEvent) {
            DetailViewEvent.OnBackClicked -> {
                viewModelScope.emitSideEffect(
                    DetailSideEffect.Navigation.NavigateBack,
                )
            }
        }
    }
}
