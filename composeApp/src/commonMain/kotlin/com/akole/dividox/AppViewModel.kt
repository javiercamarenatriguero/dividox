package com.akole.dividox

import androidx.lifecycle.ViewModel
import com.akole.dividox.AppContract.AppSideEffect
import com.akole.dividox.AppContract.AppViewEvent
import com.akole.dividox.AppContract.AppViewState
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi

class AppViewModel : ViewModel(),
    MVI<AppViewState, AppViewEvent, AppSideEffect> by mvi(AppViewState()) {

    init {
        val greeting = Greeting().greet()
        updateViewState { copy(greeting = greeting) }
    }

    override fun onViewEvent(viewEvent: AppViewEvent) {
        when (viewEvent) {
            AppViewEvent.OnButtonClicked -> onButtonClicked()
        }
    }

    private fun onButtonClicked() {
        updateViewState { copy(showContent = !showContent) }
    }
}
