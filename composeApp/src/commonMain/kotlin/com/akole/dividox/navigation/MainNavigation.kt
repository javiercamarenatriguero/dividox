package com.akole.dividox.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import dividox.common.ui_resources.generated.resources.section_dividends
import dividox.common.ui_resources.generated.resources.section_portfolio
import dividox.common.ui_resources.generated.resources.section_settings
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Serializable
data object MainGraphRoute

@Serializable
data object PortfolioRoute

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

        val currentRoute = navBackStackEntry?.destination?.route
        val selectedTab = when {
            currentRoute == DashboardRoute::class.qualifiedName -> BottomTab.DASHBOARD
            currentRoute == PortfolioRoute::class.qualifiedName -> BottomTab.PORTFOLIO
            currentRoute == DividendsRoute::class.qualifiedName -> BottomTab.DIVIDENDS
            currentRoute == SettingsRoute::class.qualifiedName -> BottomTab.SETTINGS
            else -> BottomTab.DASHBOARD
        }

        Scaffold(
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
                FloatingActionButton(onClick = { /* TK-026 */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
        ) { innerPadding ->
            NavHost(
                navController = innerNavController,
                startDestination = DashboardRoute,
                modifier = Modifier.padding(innerPadding),
            ) {
                dashboardScreenNode(rootNavController)
                portfolioScreenNode()
                dividendsScreenNode()
                settingsScreenNode()
            }
        }
    }
}

fun NavGraphBuilder.portfolioScreenNode() {
    composable<PortfolioRoute> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.section_portfolio))
        }
    }
}

fun NavGraphBuilder.dividendsScreenNode() {
    composable<DividendsRoute> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.section_dividends))
        }
    }
}

fun NavGraphBuilder.settingsScreenNode() {
    composable<SettingsRoute> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.section_settings))
        }
    }
}
