package com.akole.dividox.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardSideEffect
import com.akole.dividox.feature.dashboard.DashboardScreen
import com.akole.dividox.feature.dashboard.DashboardViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object DashboardRoute

fun NavController.navigateToDashboard(navOptions: NavOptions? = null) {
    this.navigate(DashboardRoute, navOptions)
}

fun NavGraphBuilder.dashboardScreenNode(navController: NavController, rootNavController: NavController) {
    composable<DashboardRoute> {
        val viewModel = koinViewModel<DashboardViewModel>()
        val state by collectViewState(viewModel.viewState)

        DashboardScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    is DashboardSideEffect.Navigation.NavigateToSecurity ->
                        rootNavController.navigateToSecurityDetail(ticker = navigation.ticker)
                    DashboardSideEffect.Navigation.NavigateToFavorites ->
                        rootNavController.navigateToFavorites()
                    DashboardSideEffect.Navigation.NavigateToPortfolio ->
                        navController.navigate(PortfolioRoute) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                }
            },
        )
    }
}
