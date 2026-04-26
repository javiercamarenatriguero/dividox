package com.akole.dividox.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.akole.dividox.common.auth.domain.model.SessionState
import com.akole.dividox.common.auth.domain.usecase.ObserveSessionUseCase
import com.akole.dividox.feature.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun SetupRootNavGraph(navController: NavHostController) {
    val observeSession: ObserveSessionUseCase = koinInject()
    val sessionState by observeSession().collectAsState(initial = SessionState.Loading)

    when (sessionState) {
        SessionState.Loading -> SplashScreen()
        SessionState.Unauthenticated -> {
            // TODO(TK-011): Replace with LoginRoute once feature:auth is implemented
            NavHost(navController = navController, startDestination = HomeRoute) {
                homeScreenNode(navController)
                detailScreenNode(navController)
            }
        }
        is SessionState.Authenticated -> {
            NavHost(navController = navController, startDestination = HomeRoute) {
                homeScreenNode(navController)
                detailScreenNode(navController)
            }
        }
    }
}
