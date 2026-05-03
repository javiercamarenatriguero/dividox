package com.akole.dividox.common.ui.resources.components.connectivity

import androidx.compose.runtime.compositionLocalOf
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import kotlinx.coroutines.flow.emptyFlow

val LocalNetworkConnectivityManager = compositionLocalOf<NetworkConnectivityManager> {
    error("CompositionLocal LocalNetworkConnectivityManager not provided")
}
