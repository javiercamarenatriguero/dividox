package com.akole.dividox.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.portfolio.HoldingScreen
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
    rootNavController: NavController,
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
                        rootNavController.navigateToSecurityDetail(ticker = navigation.ticker)
                    is PortfolioSideEffect.Navigation.NavigateToAddHolding ->
                        navController.navigateToAddHolding()
                    is PortfolioSideEffect.Navigation.NavigateToEditHolding ->
                        navController.navigateToEditHolding(navigation.holdingId)
                }
            },
        )
    }

    composable<AddHoldingRoute>(
        enterTransition = { slideInHorizontally { it } },
        popExitTransition = { slideOutHorizontally { it } },
    ) {
        val viewModel = koinViewModel<HoldingViewModel>(
            parameters = { parametersOf(null) }
        )

        HoldingScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
            onPositionSaved = { navController.popBackStack() },
            onPositionDeleted = { navController.popBackStack() },
        )
    }

    composable<EditHoldingRoute>(
        enterTransition = { slideInHorizontally { it } },
        popExitTransition = { slideOutHorizontally { it } },
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<EditHoldingRoute>()

        val viewModel = koinViewModel<HoldingViewModel>(
            parameters = { parametersOf(route.holdingId) }
        )

        HoldingScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
            onPositionSaved = { navController.popBackStack() },
            onPositionDeleted = { navController.popBackStack() },
        )
    }
}
