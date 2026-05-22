package com.akole.dividox.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.onboarding.OnboardingScreen
import com.akole.dividox.feature.onboarding.OnboardingSideEffect
import com.akole.dividox.feature.onboarding.OnboardingViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object OnboardingRoute

fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) {
    this.navigate(OnboardingRoute, navOptions)
}

fun NavGraphBuilder.onboardingScreenNode(navController: NavController) {
    composable<OnboardingRoute> {
        val viewModel = koinViewModel<OnboardingViewModel>()
        val state by collectViewState(viewModel.viewState)

        LaunchedEffect(Unit) {
            viewModel.sideEffect.collect { effect ->
                when (effect) {
                    OnboardingSideEffect.NavigateToDashboard -> navController.navigate(MainGraphRoute) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }

        OnboardingScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
        )
    }
}
