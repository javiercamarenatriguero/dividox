package com.akole.dividox.feature.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.feature.details.DetailContract.DetailSideEffect
import com.akole.dividox.feature.details.DetailContract.DetailViewEvent
import com.akole.dividox.feature.details.DetailContract.DetailViewState

class DetailViewModel(
    platformName: String,
    greeting: String,
) : ViewModel(),
    MVI<DetailViewState, DetailViewEvent, DetailSideEffect>
    by mvi(DetailViewState()) {

    init {
        updateViewState {
            copy(
                platformName = platformName,
                greeting = greeting,
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
