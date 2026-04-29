package com.akole.dividox.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.akole.dividox.component.auth.domain.model.SessionState
import com.akole.dividox.component.auth.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

private const val SPLASH_DURATION_MS = 2000L

@Composable
fun SetupRootNavGraph(navController: NavHostController) {
    val observeSession: ObserveSessionUseCase = koinInject()
    val sessionState by remember { observeSession() }.collectAsState(initial = SessionState.Loading)
    var splashReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        splashReady = true
    }

    LaunchedEffect(sessionState, splashReady) {
        if (!splashReady || sessionState == SessionState.Loading) return@LaunchedEffect
        when (sessionState) {
            is SessionState.Authenticated -> navController.navigate(HomeRoute) {
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
        homeScreenNode(navController)
        detailScreenNode(navController)
        dashboardScreenNode(navController)
    }
}
