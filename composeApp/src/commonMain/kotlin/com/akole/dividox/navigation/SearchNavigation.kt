package com.akole.dividox.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.search.SearchContract.SearchSideEffect.Navigation
import com.akole.dividox.feature.search.SearchScreen
import com.akole.dividox.feature.search.SearchViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/**
 * The single security search of the app.
 *
 * @property addMode `true` when opened from an "Add position" entry point: picking a result opens
 * the position form directly. `false` (explore) opens the security analysis instead.
 */
@Serializable
data class SearchRoute(val addMode: Boolean = false)

fun NavController.navigateToSearch(addMode: Boolean = false) = navigate(SearchRoute(addMode))

fun NavGraphBuilder.searchScreenNode(navController: NavController, rootNavController: NavController) {
    composable<SearchRoute> { entry ->
        val route = entry.toRoute<SearchRoute>()
        val viewModel = koinViewModel<SearchViewModel>()
        val state by collectViewState(viewModel.viewState)

        SearchScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffect = viewModel.sideEffect,
            addMode = route.addMode,
            onNavigation = { navigation ->
                when (navigation) {
                    is Navigation.NavigateToSecurity -> if (route.addMode) {
                        rootNavController.navigateToAddHolding(ticker = navigation.ticker)
                    } else {
                        rootNavController.navigateToSecurityDetail(ticker = navigation.ticker)
                    }
                    Navigation.NavigateBack ->
                        navController.popBackStack()
                }
            },
        )
    }
}
