package com.akole.dividox.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.akole.dividox.component.auth.domain.model.SessionState
import com.akole.dividox.component.auth.domain.usecase.ObserveSessionUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.take
import org.koin.compose.koinInject

private const val SPLASH_DURATION_MS = 2000L

@Composable
fun SetupRootNavGraph(navController: NavHostController) {
    val observeSession: ObserveSessionUseCase = koinInject()
    val getPortfolioWithQuotes: GetPortfolioWithQuotesUseCase = koinInject()
    val sessionState by retain { observeSession() }.collectAsState(initial = SessionState.Loading)
    var splashReady by retain { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        splashReady = true
    }

    // Warm up the quote cache while the splash is visible — only when authenticated.
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Authenticated) {
            getPortfolioWithQuotes()
                .take(1)
                .catch { }
                .collect { }
        }
    }

    var lastHandledSessionState by retain { mutableStateOf<SessionState>(SessionState.Loading) }

    LaunchedEffect(sessionState, splashReady) {
        if (!splashReady || sessionState == SessionState.Loading) return@LaunchedEffect
        if (sessionState == lastHandledSessionState) return@LaunchedEffect
        lastHandledSessionState = sessionState
        when (sessionState) {
            is SessionState.Authenticated -> navController.navigate(MainGraphRoute) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            SessionState.Unauthenticated -> navController.navigate(LoginRoute) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            SessionState.Loading -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } },
    ) {
        splashScreenNode()
        loginScreenNode(navController)
        signUpScreenNode(navController)
        forgotPasswordScreenNode(navController)
        mainGraphNode(navController)
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
}
