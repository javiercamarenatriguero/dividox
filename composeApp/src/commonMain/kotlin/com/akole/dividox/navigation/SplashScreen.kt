package com.akole.dividox.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.akole.dividox.feature.splash.SplashScreen
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

fun NavGraphBuilder.splashScreenNode() {
    composable<SplashRoute> {
        SplashScreen()
    }
}
