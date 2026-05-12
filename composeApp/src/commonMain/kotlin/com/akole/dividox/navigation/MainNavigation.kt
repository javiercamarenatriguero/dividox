package com.akole.dividox.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.akole.dividox.common.ui.resources.components.BottomTab
import com.akole.dividox.common.ui.resources.components.DividoxBottomBar
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.portfolio_add_holding
import dividox.common.ui_resources.generated.resources.section_dividends
import dividox.common.ui_resources.generated.resources.section_portfolio
import dividox.common.ui_resources.generated.resources.section_settings
import com.akole.dividox.common.mvi.collectViewState
import com.akole.dividox.feature.dividends.DividendsContract.DividendsSideEffect
import com.akole.dividox.feature.dividends.DividendsScreen
import com.akole.dividox.feature.dividends.DividendsViewModel
import com.akole.dividox.common.settings.data.share.FileShareService
import com.akole.dividox.feature.settings.SettingsScreen
import com.akole.dividox.feature.settings.SettingsViewEvent
import com.akole.dividox.feature.settings.SettingsViewModel
import com.akole.dividox.feature.settings.SettingsViewSideEffect
import com.akole.dividox.common.mvi.collectViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object MainGraphRoute

@Serializable
data object PortfolioRoute

@Serializable
data class AddHoldingRoute(val ticker: String? = null)

@Serializable
data class EditHoldingRoute(val holdingId: String)

@Serializable
data object DividendsRoute

@Serializable
data object SettingsRoute

fun NavController.navigateToMain(navOptions: NavOptions? = null) {
    this.navigate(MainGraphRoute, navOptions)
}

fun NavGraphBuilder.mainGraphNode(rootNavController: NavController) {
    composable<MainGraphRoute> {
        val innerNavController = rememberNavController()
        val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
        var portfolioFabClick by retain { mutableStateOf({}) }

        val currentRoute = navBackStackEntry?.destination?.route
        val selectedTab = when {
            currentRoute?.contains(DashboardRoute::class.simpleName ?: "") == true -> BottomTab.DASHBOARD
            currentRoute?.contains(PortfolioRoute::class.simpleName ?: "") == true -> BottomTab.PORTFOLIO
            currentRoute?.contains(DividendsRoute::class.simpleName ?: "") == true -> BottomTab.DIVIDENDS
            currentRoute?.contains(SettingsRoute::class.simpleName ?: "") == true -> BottomTab.SETTINGS
            else -> BottomTab.DASHBOARD
        }

        Scaffold(
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                DividoxBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        val route = when (tab) {
                            BottomTab.DASHBOARD -> DashboardRoute
                            BottomTab.PORTFOLIO -> PortfolioRoute
                            BottomTab.DIVIDENDS -> DividendsRoute
                            BottomTab.SETTINGS -> SettingsRoute
                        }
                        innerNavController.navigate(route) {
                            popUpTo(innerNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            },
            floatingActionButton = {
                when (selectedTab) {
                    BottomTab.DASHBOARD -> {
                        FloatingActionButton(onClick = { rootNavController.navigate(SearchRoute) }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                            )
                        }
                    }
                    BottomTab.PORTFOLIO -> {
                        FloatingActionButton(onClick = portfolioFabClick) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(Res.string.portfolio_add_holding),
                            )
                        }
                    }
                    else -> {}
                }
            },
            floatingActionButtonPosition = FabPosition.End,
        ) { innerPadding ->
            NavHost(
                navController = innerNavController,
                startDestination = DashboardRoute,
                modifier = Modifier.padding(innerPadding),
            ) {
                dashboardScreenNode(navController = innerNavController, rootNavController = rootNavController)
                portfolioScreenNode(
                    navController = innerNavController,
                    rootNavController = rootNavController,
                    onRegisterFabClick = { callback -> portfolioFabClick = callback },
                )
                dividendsScreenNode(navController = innerNavController, rootNavController = rootNavController)
                settingsScreenNode(navController = innerNavController, rootNavController = rootNavController)
            }
        }
    }
}

fun NavGraphBuilder.dividendsScreenNode(navController: NavController, rootNavController: NavController) {
    composable<DividendsRoute> {
        val viewModel = koinViewModel<DividendsViewModel>()
        val state by collectViewState(viewModel.viewState)

        DividendsScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    is DividendsSideEffect.Navigation.NavigateToSecurity ->
                        rootNavController.navigateToSecurityDetail(ticker = navigation.ticker)
                }
            },
        )
    }
}

fun NavGraphBuilder.settingsScreenNode(navController: NavController, rootNavController: NavController) {
    composable<SettingsRoute> {
        val viewModel = koinViewModel<SettingsViewModel>()
        val state by collectViewState(viewModel.viewState)
        val fileShareService: FileShareService = koinInject()
        val uriHandler = LocalUriHandler.current

        SettingsScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
        )

        LaunchedEffect(Unit) {
            viewModel.sideEffect.collect { effect ->
                when (effect) {
                    is SettingsViewSideEffect.Navigation.NavigateToFavorites ->
                        rootNavController.navigateToFavorites()
                    is SettingsViewSideEffect.Navigation.NavigateToLogin ->
                        rootNavController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    is SettingsViewSideEffect.LaunchShareSheet ->
                        withContext(Dispatchers.Default) {
                            fileShareService.share("dividox_portfolio.csv", effect.csvContent)
                        }
                    is SettingsViewSideEffect.OpenUrl ->
                        runCatching { uriHandler.openUri(effect.url) }
                    else -> {}
                }
            }
        }
    }
}
