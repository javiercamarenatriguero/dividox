package com.akole.dividox.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import com.akole.dividox.common.auth.domain.model.SessionState
import com.akole.dividox.common.auth.domain.usecase.ObserveSessionUseCase
import com.akole.dividox.feature.auth.navigation.LoginRoute
import com.akole.dividox.feature.auth.navigation.SignUpRoute
import com.akole.dividox.feature.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun SetupRootNavGraph(navController: NavHostController) {
    val observeSession: ObserveSessionUseCase = koinInject()
    val sessionState by observeSession().collectAsState(initial = SessionState.Loading)

    when (sessionState) {
        SessionState.Loading -> SplashScreen()
        SessionState.Unauthenticated -> {
            NavHost(navController = navController, startDestination = LoginRoute) {
                loginScreenNode(
                    onNavigateToSignUp = { navController.navigateToSignUp() },
                    onNavigateToHome = {
                        navController.navigateToHome(
                            navOptions = navOptions {
                                popUpTo(LoginRoute) { inclusive = true }
                            },
                        )
                    },
                )
                signUpScreenNode(
                    onNavigateToLogin = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigateToHome(
                            navOptions = navOptions {
                                popUpTo(SignUpRoute) { inclusive = true }
                            },
                        )
                    },
                )
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
