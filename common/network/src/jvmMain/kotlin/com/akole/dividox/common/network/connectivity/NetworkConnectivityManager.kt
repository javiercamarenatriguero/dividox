package com.akole.dividox.common.network.connectivity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import java.net.InetAddress

/**
 * JVM (Desktop) implementation of [NetworkConnectivityManager].
 *
 * Uses polling via [InetAddress.isReachable] to detect connectivity.
 * Suitable for desktop applications where system-level APIs are not available.
 * Polls every 10 seconds and emits state changes with 500ms debounce.
 *
 * **Platform Details:**
 * - Polls Google DNS (8.8.8.8) every 10s for connectivity check (conservative to avoid CPU overhead)
 * - Uses timeout (3s) for isReachable()
 * - Runs on Dispatchers.IO to avoid blocking main thread
 * - Auto-detects initial state on subscription
 */
actual class NetworkConnectivityManager {

    actual fun observeConnectivity(): Flow<Boolean> {
        return callbackFlow {
            while (true) {
                val isConnected = withContext(Dispatchers.IO) {
                    try {
                        InetAddress.getByName("8.8.8.8").isReachable(3000)
                    } catch (e: Exception) {
                        false
                    }
                }
                trySend(isConnected)
                delay(10000) // Poll every 10s for efficiency
            }

            @Suppress("UNREACHABLE_CODE")
            awaitClose { }
        }
            .debounce(500)
            .distinctUntilChanged()
    }
}
