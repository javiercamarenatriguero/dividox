package com.akole.dividox.common.network.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Android implementation of [NetworkConnectivityManager].
 *
 * Uses [ConnectivityManager.registerNetworkCallback] to listen for network state changes.
 * Emits connectivity state as a Flow<Boolean> with 500ms debounce to prevent flapping.
 *
 * **Platform Details:**
 * - API 24+ compatible (uses NetworkCallback)
 * - Registers callback for all transports (WiFi, Cellular, etc.)
 * - Auto-detects initial connectivity state on subscription
 * - Properly cleans up callback on Flow cancellation
 */
actual class NetworkConnectivityManager(private val context: Context) {

    actual fun observeConnectivity(): Flow<Boolean> {
        return callbackFlow {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // Emit initial state
            val initialState = isConnected(connectivityManager)
            trySend(initialState)

            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    trySend(true)
                }

                override fun onLost(network: Network) {
                    trySend(false)
                }

                override fun onUnavailable() {
                    trySend(false)
                }
            }

            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        }
            .debounce(500)
            .distinctUntilChanged()
    }

    private fun isConnected(connectivityManager: ConnectivityManager): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
