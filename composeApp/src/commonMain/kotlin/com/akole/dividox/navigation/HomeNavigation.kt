package com.akole.dividox.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.akole.dividox.AppContract.AppSideEffect
import com.akole.dividox.AppContract.AppViewEvent
import com.akole.dividox.AppScreen
import com.akole.dividox.AppViewModel
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.getPlatform

fun NavGraphBuilder.homeScreenNode(navController: NavController) {
    composable<HomeRoute> {
        val viewModel = androidx.lifecycle.viewmodel.compose.viewModel { AppViewModel() }
        val state by collectViewState(viewModel.viewState)

        CollectSideEffect(viewModel.sideEffect) { effect ->
            when (effect) {
                is AppSideEffect.Navigation -> handleHomeNavigation(effect, navController)
            }
        }

        AppScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    AppViewEvent.OnButtonClicked -> viewModel.onViewEvent(event)
                    AppViewEvent.OnDetailClicked -> {
                        navController.navigate(DetailRoute(platformName = getPlatform().name))
                    }
                }
            },
        )
    }
}

private fun handleHomeNavigation(
    effect: AppSideEffect.Navigation,
    navController: NavController,
) {
    // Handle future navigation side effects here
}
