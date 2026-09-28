package com.akole.dividox.common.network.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlin.time.Duration.Companion.seconds

private const val STOP_SHARING_TIMEOUT_MS = 5_000L

/**
 * Android implementation of [NetworkConnectivityManager].
 *
 * Uses [ConnectivityManager.registerNetworkCallback] and tracks **every** network that provides
 * internet: callbacks are per network, so `onLost` for one of them (mobile data torn down after
 * Wi-Fi connects, Wi-Fi ↔ cellular handover, VPN) only means offline when no other network is left.
 *
 * A single callback is shared by all collectors (screens) and kept for 5s after the last one leaves,
 * so tab switches don't re-register it. Offline emissions are debounced (1s).
 */
actual class NetworkConnectivityManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val connectivity: Flow<Boolean> = callbackFlow {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networks = ActiveNetworks<Network>()

        trySend(isConnected(connectivityManager))

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(networks.add(network))
            }

            override fun onLost(network: Network) {
                trySend(networks.remove(network))
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
        .debounceOfflineOnly(1.seconds)
        .shareIn(scope, SharingStarted.WhileSubscribed(STOP_SHARING_TIMEOUT_MS), replay = 1)

    actual fun observeConnectivity(): Flow<Boolean> = connectivity

    private fun isConnected(connectivityManager: ConnectivityManager): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
