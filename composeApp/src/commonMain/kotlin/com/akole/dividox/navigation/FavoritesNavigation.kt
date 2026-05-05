package com.akole.dividox.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesSideEffect.Navigation
import com.akole.dividox.feature.favorites.FavoritesScreen
import com.akole.dividox.feature.favorites.FavoritesViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object FavoritesRoute

fun NavGraphBuilder.favoritesScreenNode(navController: NavController, rootNavController: NavController) {
    composable<FavoritesRoute> {
        val viewModel = koinViewModel<FavoritesViewModel>()
        val state by collectViewState(viewModel.viewState)

        FavoritesScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffect = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    is Navigation.NavigateToSecurity ->
                        rootNavController.navigateToSecurityDetail(ticker = navigation.ticker)
                    Navigation.NavigateBack ->
                        navController.popBackStack()
                }
            },
        )
    }
}
