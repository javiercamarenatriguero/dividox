package com.akole.dividox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.ui.resources.components.connectivity.LocalNetworkConnectivityManager
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.navigation.SetupRootNavGraph
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    DividoxTheme {
        val navController = rememberNavController()
        val connectivityManager: NetworkConnectivityManager = koinInject()

        CompositionLocalProvider(LocalNetworkConnectivityManager provides connectivityManager) {
            Box(modifier = Modifier.fillMaxSize()) {
                SetupRootNavGraph(navController)
            }
        }
    }
}
