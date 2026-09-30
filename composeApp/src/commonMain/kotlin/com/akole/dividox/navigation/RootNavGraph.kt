package com.akole.dividox.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.akole.dividox.common.ui.resources.components.BottomTab
import com.akole.dividox.feature.portfolio.PositionActions
import com.akole.dividox.component.auth.domain.model.SessionState
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.component.auth.domain.usecase.ObserveSessionUseCase
import com.akole.dividox.component.market.domain.usecase.GetMajorMarketIndicesUseCase
import com.akole.dividox.component.market.domain.usecase.GetMarketNewsUseCase
import com.akole.dividox.integration.security.domain.usecase.GetEnrichedWatchlistUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import org.koin.compose.koinInject

private const val SPLASH_DURATION_MS = 2000L

@Composable
fun SetupRootNavGraph(navController: NavHostController) {
    val observeSession: ObserveSessionUseCase = koinInject()
    val observeAppSettings: ObserveAppSettingsUseCase = koinInject()
    val getPortfolioWithQuotes: GetPortfolioWithQuotesUseCase = koinInject()
    val getEnrichedWatchlist: GetEnrichedWatchlistUseCase = koinInject()
    val getMajorMarketIndices: GetMajorMarketIndicesUseCase = koinInject()
    val getMarketNews: GetMarketNewsUseCase = koinInject()
    val sessionState by retain { observeSession() }.collectAsState(initial = SessionState.Loading)
    val appSettings by retain { observeAppSettings() }.collectAsState(initial = null)
    var splashReady by retain { mutableStateOf(false) }
    val positionActions: PositionActions = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }
    val tabRequests = remember { MutableStateFlow<BottomTab?>(null) }
    val rootEntry by navController.currentBackStackEntryAsState()
    val isMainGraphVisible = rootEntry?.destination?.hasRoute<MainGraphRoute>() == true
    val onViewPortfolio: () -> Unit = {
        navController.popBackStack<MainGraphRoute>(inclusive = false)
        tabRequests.value = BottomTab.PORTFOLIO
    }

    CollectPositionFeedback(positionActions, snackbarHostState)

    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        splashReady = true
    }

    // Warm up dashboard caches in parallel while the splash is visible — only when
    // authenticated. Every branch is best-effort: failures are swallowed so the splash
    // never blocks on a slow API.
    LaunchedEffect(sessionState) {
        if (sessionState !is SessionState.Authenticated) return@LaunchedEffect
        val defaultMarket = runCatching { observeAppSettings().first().defaultMarket }
            .getOrNull()
        coroutineScope {
            async {
                // take(1) only: pre-warm quote cache (Phase 1). Do NOT wait for Phase 2
                // (dividend enrichment) — its N parallel calls would saturate the shared
                // apiSemaphore (max 8) and block the dashboard's own quote fetches. Phase 2
                // runs later inside the dashboard's own subscription and populates the
                // DividendInfo Room cache for subsequent cold starts.
                runCatching {
                    getPortfolioWithQuotes().take(1).catch { }.collect { }
                }
            }
            async {
                runCatching {
                    getEnrichedWatchlist().take(1).catch { }.collect { }
                }
            }
            if (defaultMarket != null) {
                async { runCatching { getMajorMarketIndices(defaultMarket) } }
                async { runCatching { getMarketNews(defaultMarket) } }
            }
        }
    }

    var lastHandledSessionState by retain { mutableStateOf<SessionState>(SessionState.Loading) }

    LaunchedEffect(sessionState, splashReady, appSettings) {
        if (!splashReady || sessionState == SessionState.Loading) return@LaunchedEffect
        if (sessionState == lastHandledSessionState) return@LaunchedEffect
        when (sessionState) {
            is SessionState.Authenticated -> {
                val settings = appSettings ?: return@LaunchedEffect
                lastHandledSessionState = sessionState
                if (settings.onboardingCompleted) {
                    navController.navigate(MainGraphRoute) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    navController.navigate(OnboardingRoute) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            SessionState.Unauthenticated -> {
                lastHandledSessionState = sessionState
                navController.navigate(LoginRoute) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            SessionState.Loading -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = SplashRoute,
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { -it } },
            popEnterTransition = { slideInHorizontally { -it } },
            popExitTransition = { slideOutHorizontally { it } },
        ) {
            splashScreenNode()
            onboardingScreenNode(navController)
            loginScreenNode(navController)
            signUpScreenNode(navController)
            forgotPasswordScreenNode(navController)
            mainGraphNode(
                rootNavController = navController,
                snackbarHostState = snackbarHostState,
                tabRequests = tabRequests,
                onViewPortfolio = onViewPortfolio,
            )
            detailScreenNode(navController)
            securityDetailScreenNode(navController)
            searchScreenNode(navController = navController, rootNavController = navController)
            favoritesScreenNode(navController = navController, rootNavController = navController)
            addHoldingScreenNode(navController)
            editHoldingScreenNode(navController)
            aboutScreenNode(navController)
            termsScreenNode(navController)
            privacyScreenNode(navController)
        }
        // The main graph hosts the snackbar in its own Scaffold (above the bottom bar and FAB).
        if (!isMainGraphVisible) {
            PositionSnackbarHost(
                hostState = snackbarHostState,
                onViewPortfolio = onViewPortfolio,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
            )
        }
    }
}
