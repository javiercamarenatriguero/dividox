package com.akole.dividox.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.portfolio.PortfolioContract
import com.akole.dividox.feature.portfolio.PortfolioContract.PortfolioSideEffect
import com.akole.dividox.feature.portfolio.PortfolioScreen
import com.akole.dividox.feature.portfolio.PortfolioViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavController.navigateToPortfolio(navOptions: NavOptions? = null) {
    this.navigate(PortfolioRoute, navOptions)
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
                    is PortfolioSideEffect.Navigation.NavigateToAddHolding -> {
                        // TODO: Wire in TK-020 (AddHoldingRoute)
                    }
                    is PortfolioSideEffect.Navigation.NavigateToEditHolding -> {
                        // TODO: Wire in TK-020 (EditHoldingRoute)
                    }
                }
            },
        )
    }
}
