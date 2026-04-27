package com.akole.dividox.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.auth.domain.usecase.ForgotPasswordUseCase
import com.akole.dividox.common.auth.domain.usecase.SignInWithEmailUseCase
import com.akole.dividox.common.auth.domain.usecase.SignInWithGoogleUseCase
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.feature.auth.login.LoginContract.LoginSideEffect
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewEvent
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewEvent.OnEmailChanged
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewEvent.OnErrorDismissed
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewEvent.OnForgotPasswordClicked
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewEvent.OnGoogleSignInClicked
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewEvent.OnPasswordChanged
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewEvent.OnSignInClicked
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewEvent.OnSignUpClicked
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewState
import kotlinx.coroutines.launch

class LoginViewModel(
    private val signInWithEmail: SignInWithEmailUseCase,
    @Suppress("UnusedPrivateMember")
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val forgotPassword: ForgotPasswordUseCase,
) : ViewModel(),
    MVI<LoginViewState, LoginViewEvent, LoginSideEffect> by mvi(LoginViewState()) {

    override fun onViewEvent(viewEvent: LoginViewEvent) {
        when (viewEvent) {
            is OnEmailChanged -> updateViewState { copy(email = viewEvent.email) }
            is OnPasswordChanged -> updateViewState { copy(password = viewEvent.password) }
            OnSignInClicked -> signIn()
            OnGoogleSignInClicked -> {
                // TODO(TK-013): Google IdToken flow — requires platform-specific launcher
            }
            OnForgotPasswordClicked -> viewModelScope.emitSideEffect(
                LoginSideEffect.ShowForgotPasswordDialog(viewState.value.email),
            )
            OnSignUpClicked -> viewModelScope.emitSideEffect(
                LoginSideEffect.Navigation.NavigateToSignUp,
            )
            OnErrorDismissed -> updateViewState { copy(error = null) }
        }
    }

    private fun signIn() {
        viewModelScope.launch {
            updateViewState { copy(isLoading = true, error = null) }
            signInWithEmail(viewState.value.email, viewState.value.password)
                .onSuccess { viewModelScope.emitSideEffect(LoginSideEffect.Navigation.NavigateToHome) }
                .onFailure { updateViewState { copy(error = it.message) } }
            updateViewState { copy(isLoading = false) }
        }
    }
}
