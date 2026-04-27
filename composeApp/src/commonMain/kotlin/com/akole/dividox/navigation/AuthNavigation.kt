package com.akole.dividox.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.auth.login.LoginContract.LoginSideEffect
import com.akole.dividox.feature.auth.login.LoginScreen
import com.akole.dividox.feature.auth.login.LoginViewModel
import com.akole.dividox.feature.auth.navigation.LoginRoute
import com.akole.dividox.feature.auth.navigation.SignUpRoute
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpSideEffect
import com.akole.dividox.feature.auth.register.SignUpScreen
import com.akole.dividox.feature.auth.register.SignUpViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavController.navigateToLogin(navOptions: NavOptions? = null) {
    this.navigate(LoginRoute, navOptions)
}

fun NavController.navigateToSignUp(navOptions: NavOptions? = null) {
    this.navigate(SignUpRoute, navOptions)
}

fun NavGraphBuilder.loginScreenNode(
    onNavigateToSignUp: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    composable<LoginRoute> {
        val viewModel = koinViewModel<LoginViewModel>()
        val state by collectViewState(viewModel.viewState)

        LoginScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    LoginSideEffect.Navigation.NavigateToSignUp -> onNavigateToSignUp()
                    LoginSideEffect.Navigation.NavigateToHome -> onNavigateToHome()
                }
            },
        )
    }
}

fun NavGraphBuilder.signUpScreenNode(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    composable<SignUpRoute> {
        val viewModel = koinViewModel<SignUpViewModel>()
        val state by collectViewState(viewModel.viewState)

        SignUpScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    SignUpSideEffect.Navigation.NavigateToLogin -> onNavigateToLogin()
                    SignUpSideEffect.Navigation.NavigateToHome -> onNavigateToHome()
                }
            },
        )
    }
}
