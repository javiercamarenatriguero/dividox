package com.akole.dividox.feature.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.component.auth.domain.usecase.ForgotPasswordUseCase
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordSideEffect
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordViewEvent
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordViewEvent.OnBackClicked
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordViewEvent.OnEmailChanged
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordViewEvent.OnSendResetLinkClicked
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordViewState
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val forgotPassword: ForgotPasswordUseCase,
) : ViewModel(),
    MVI<ForgotPasswordViewState, ForgotPasswordViewEvent, ForgotPasswordSideEffect>
    by mvi(ForgotPasswordViewState()) {

    override fun onViewEvent(viewEvent: ForgotPasswordViewEvent) {
        when (viewEvent) {
            is OnEmailChanged -> updateViewState { copy(email = viewEvent.email) }
            OnSendResetLinkClicked -> sendResetLink()
            OnBackClicked -> viewModelScope.emitSideEffect(ForgotPasswordSideEffect.Navigation.NavigateBack)
        }
    }

    private fun sendResetLink() {
        viewModelScope.launch {
            updateViewState { copy(isLoading = true, error = null) }
            forgotPassword(viewState.value.email)
                .onSuccess { updateViewState { copy(isLoading = false, isSuccess = true) } }
                .onFailure { updateViewState { copy(isLoading = false, error = it.message) } }
        }
    }
}
