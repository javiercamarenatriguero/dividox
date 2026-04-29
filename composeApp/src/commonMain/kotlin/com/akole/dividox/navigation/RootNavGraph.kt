package com.akole.dividox.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.akole.dividox.component.auth.domain.model.SessionState
import com.akole.dividox.component.auth.domain.usecase.ObserveSessionUseCase
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.section_favourites
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
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
        composable<FavoritesRoute> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.section_favourites))
            }
        }
    }
}
