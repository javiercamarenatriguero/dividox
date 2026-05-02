package com.akole.dividox.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.portfolio.HoldingSheet
import com.akole.dividox.feature.portfolio.HoldingViewModel
import com.akole.dividox.feature.portfolio.PortfolioContract
import com.akole.dividox.feature.portfolio.PortfolioContract.PortfolioSideEffect
import com.akole.dividox.feature.portfolio.PortfolioScreen
import com.akole.dividox.feature.portfolio.PortfolioViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToPortfolio(navOptions: NavOptions? = null) {
    this.navigate(PortfolioRoute, navOptions)
}

fun NavController.navigateToAddHolding() {
    this.navigate(AddHoldingRoute)
}

fun NavController.navigateToEditHolding(holdingId: String) {
    this.navigate(EditHoldingRoute(holdingId))
}

fun NavGraphBuilder.portfolioScreenNode(
    navController: NavController,
    onRegisterFabClick: ((() -> Unit) -> Unit) = {},
) {
    composable<PortfolioRoute> {
        val viewModel = koinViewModel<PortfolioViewModel>()
        val state by collectViewState(viewModel.viewState)

        onRegisterFabClick {
            viewModel.onViewEvent(PortfolioContract.PortfolioViewEvent.AddHoldingClicked)
        }

        PortfolioScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    is PortfolioSideEffect.Navigation.NavigateToSecurity ->
                        navController.navigate(SecurityDetailRoute(ticker = navigation.ticker))
                    is PortfolioSideEffect.Navigation.NavigateToAddHolding ->
                        navController.navigateToAddHolding()
                    is PortfolioSideEffect.Navigation.NavigateToEditHolding ->
                        navController.navigateToEditHolding(navigation.holdingId)
                }
            },
        )
    }

    // Add Holding Sheet
    composable<AddHoldingRoute> {
        val viewModel = koinViewModel<HoldingViewModel>(
            parameters = { parametersOf(null) }
        )
        val state by collectViewState(viewModel.viewState)

        HoldingSheet(
            viewModel = viewModel,
            onDismiss = { navController.popBackStack() },
            onPositionSaved = { navController.popBackStack() },
            onPositionDeleted = { navController.popBackStack() },
        )
    }

    // Edit Holding Sheet
    composable<EditHoldingRoute> { backStackEntry ->
        val route = backStackEntry.destination.route as? EditHoldingRoute ?: return@composable
        
        val viewModel = koinViewModel<HoldingViewModel>(
            parameters = { parametersOf(route.holdingId) }
        )
        val state by collectViewState(viewModel.viewState)

        HoldingSheet(
            viewModel = viewModel,
            onDismiss = { navController.popBackStack() },
            onPositionSaved = { navController.popBackStack() },
            onPositionDeleted = { navController.popBackStack() },
        )
    }
}
