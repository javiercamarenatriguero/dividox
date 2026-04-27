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
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpSideEffect
import com.akole.dividox.feature.auth.register.SignUpScreen
import com.akole.dividox.feature.auth.register.SignUpViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object LoginRoute

@Serializable
data object SignUpRoute

fun NavController.navigateToLogin(navOptions: NavOptions? = null) {
    this.navigate(LoginRoute, navOptions)
}

fun NavController.navigateToSignUp(navOptions: NavOptions? = null) {
    this.navigate(SignUpRoute, navOptions)
}

fun NavGraphBuilder.loginScreenNode(navController: NavController) {
    composable<LoginRoute> {
        val viewModel = koinViewModel<LoginViewModel>()
        val state by collectViewState(viewModel.viewState)

        LoginScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    LoginSideEffect.Navigation.NavigateToSignUp ->
                        navController.navigateToSignUp()
                }
            },
        )
    }
}

fun NavGraphBuilder.signUpScreenNode(navController: NavController) {
    composable<SignUpRoute> {
        val viewModel = koinViewModel<SignUpViewModel>()
        val state by collectViewState(viewModel.viewState)

        SignUpScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    SignUpSideEffect.Navigation.NavigateToLogin ->
                        navController.popBackStack()
                }
            },
        )
    }
}
