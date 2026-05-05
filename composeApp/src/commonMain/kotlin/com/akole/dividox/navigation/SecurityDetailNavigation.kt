package com.akole.dividox.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.analysis.SecurityDetailContract.SecurityDetailSideEffect
import com.akole.dividox.feature.analysis.SecurityDetailScreen
import com.akole.dividox.feature.analysis.SecurityDetailViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class SecurityDetailRoute(val ticker: String)

fun NavController.navigateToSecurityDetail(ticker: String, navOptions: NavOptions? = null) {
    this.navigate(SecurityDetailRoute(ticker = ticker), navOptions)
}

fun NavGraphBuilder.securityDetailScreenNode(navController: NavController) {
    composable<SecurityDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<SecurityDetailRoute>()
        val viewModel = koinViewModel<SecurityDetailViewModel>(
            parameters = { parametersOf(route.ticker) },
        )
        val state by collectViewState(viewModel.viewState)

        SecurityDetailScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    SecurityDetailSideEffect.Navigation.NavigateBack ->
                        navController.popBackStack()
                    is SecurityDetailSideEffect.Navigation.NavigateToAddSecurity ->
                        navController.navigateToAddHolding(ticker = navigation.ticker)
                    is SecurityDetailSideEffect.Navigation.NavigateToEditHolding ->
                        navController.navigateToEditHolding(navigation.holdingId.value)
                }
            },
        )
    }
}
