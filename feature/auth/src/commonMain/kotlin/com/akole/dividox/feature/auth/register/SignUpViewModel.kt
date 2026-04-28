package com.akole.dividox.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.component.auth.domain.usecase.SignUpWithEmailUseCase
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpSideEffect
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewEvent
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewEvent.OnCreateAccountClicked
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewEvent.OnEmailChanged
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewEvent.OnErrorDismissed
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewEvent.OnNameChanged
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewEvent.OnPasswordChanged
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewEvent.OnSignInClicked
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewEvent.OnTermsChanged
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewState
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val signUpWithEmail: SignUpWithEmailUseCase,
) : ViewModel(),
    MVI<SignUpViewState, SignUpViewEvent, SignUpSideEffect> by mvi(SignUpViewState()) {

    override fun onViewEvent(viewEvent: SignUpViewEvent) {
        when (viewEvent) {
            is OnNameChanged -> updateViewState { copy(name = viewEvent.name) }
            is OnEmailChanged -> updateViewState { copy(email = viewEvent.email) }
            is OnPasswordChanged -> updateViewState { copy(password = viewEvent.password) }
            is OnTermsChanged -> updateViewState { copy(termsAccepted = viewEvent.accepted) }
            OnCreateAccountClicked -> createAccount()
            OnSignInClicked -> viewModelScope.emitSideEffect(SignUpSideEffect.Navigation.NavigateToLogin)
            OnErrorDismissed -> updateViewState { copy(error = null) }
        }
    }

    private fun createAccount() {
        val current = viewState.value
        if (!current.termsAccepted) {
            updateViewState { copy(error = "You must accept the Terms of Service to continue.") }
            return
        }
        viewModelScope.launch {
            updateViewState { copy(isLoading = true, error = null) }
            signUpWithEmail(current.email, current.password)
                .onFailure { updateViewState { copy(isLoading = false, error = it.message) } }
            // On success: keep isLoading=true — ObserveSessionUseCase fires Authenticated
            // which triggers RootNavGraph to switch to the home graph.
        }
    }
}
