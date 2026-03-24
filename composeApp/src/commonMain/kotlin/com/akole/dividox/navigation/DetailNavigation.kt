package com.akole.dividox.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.detail.DetailContract.DetailSideEffect
import com.akole.dividox.detail.DetailScreen
import com.akole.dividox.detail.DetailViewModel

fun NavGraphBuilder.detailScreenNode(navController: NavController) {
    composable<DetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<DetailRoute>()
        val viewModel = androidx.lifecycle.viewmodel.compose.viewModel {
            DetailViewModel(platformName = route.platformName)
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
