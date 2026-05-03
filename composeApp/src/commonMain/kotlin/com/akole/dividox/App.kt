package com.akole.dividox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.ui.resources.components.connectivity.ConnectivityBannerHost
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.navigation.SetupRootNavGraph
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    DividoxTheme {
        val navController = rememberNavController()
        val connectivityManager: NetworkConnectivityManager = koinInject()

        Column(modifier = Modifier.fillMaxSize()) {
            ConnectivityBannerHost(connectivityFlow = connectivityManager.observeConnectivity())
            SetupRootNavGraph(navController)
        }
    }
}
