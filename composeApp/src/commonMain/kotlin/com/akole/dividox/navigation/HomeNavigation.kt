package com.akole.dividox.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.akole.dividox.Greeting
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.home.HomeContract.HomeSideEffect
import com.akole.dividox.feature.home.HomeScreen
import com.akole.dividox.feature.home.HomeViewModel
import com.akole.dividox.getPlatform
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data object HomeRoute

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(HomeRoute, navOptions)
}

fun NavGraphBuilder.homeScreenNode(navController: NavController) {
    composable<HomeRoute> {
        val greeting = Greeting().greet()
        val platformName = getPlatform().name
        val viewModel = koinViewModel<HomeViewModel>(
            parameters = { parametersOf(greeting, platformName) },
        )
        val state by collectViewState(viewModel.viewState)

        HomeScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    is HomeSideEffect.Navigation.NavigateToDetail ->
                        navController.navigateToDetail(platformName = navigation.platformName)
                }
            },
        )
    }
}
