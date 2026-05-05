package com.akole.dividox.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.search.SearchContract.SearchSideEffect.Navigation
import com.akole.dividox.feature.search.SearchScreen
import com.akole.dividox.feature.search.SearchViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object SearchRoute

fun NavGraphBuilder.searchScreenNode(navController: NavController, rootNavController: NavController) {
    composable<SearchRoute> {
        val viewModel = koinViewModel<SearchViewModel>()
        val state by collectViewState(viewModel.viewState)

        SearchScreen(
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
