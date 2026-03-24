package com.akole.dividox.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.akole.dividox.Greeting
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.details.DetailContract.DetailSideEffect
import com.akole.dividox.feature.details.DetailScreen
import com.akole.dividox.feature.details.DetailViewModel
import kotlinx.serialization.Serializable

@Serializable
data class DetailRoute(val platformName: String)

fun NavController.navigateToDetail(platformName: String, navOptions: NavOptions? = null) {
    this.navigate(DetailRoute(platformName = platformName), navOptions)
}

fun NavGraphBuilder.detailScreenNode(navController: NavController) {
    composable<DetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<DetailRoute>()
        val greeting = Greeting().greet()
        val viewModel = androidx.lifecycle.viewmodel.compose.viewModel {
            DetailViewModel(platformName = route.platformName, greeting = greeting)
        }
        val state by collectViewState(viewModel.viewState)

        DetailScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    DetailSideEffect.Navigation.NavigateBack ->
                        navController.popBackStack()
                }
            },
        )
    }
}
