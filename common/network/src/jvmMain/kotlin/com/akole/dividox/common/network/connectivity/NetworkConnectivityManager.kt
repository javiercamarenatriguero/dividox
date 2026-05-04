package com.akole.dividox.common.network.connectivity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/**
 * JVM (Desktop) implementation of [NetworkConnectivityManager].
 *
 * Uses TCP socket polling to detect connectivity (ICMP/isReachable requires root on most JVMs).
 * Connects to Google DNS port 53 (TCP) which works without elevated privileges.
 * Polls every 10 seconds and emits state changes.
 */
actual class NetworkConnectivityManager {

    actual fun observeConnectivity(): Flow<Boolean> {
        return callbackFlow {
            while (true) {
                val isConnected = withContext(Dispatchers.IO) {
                    try {
                        Socket().use { socket ->
                            socket.connect(InetSocketAddress("8.8.8.8", 53), 3000)
                            true
                        }
                    } catch (e: Exception) {
                        false
                    }
                }
                trySend(isConnected)
                delay(10_000)
            }

            @Suppress("UNREACHABLE_CODE")
            awaitClose { }
        }
            .distinctUntilChanged()
    }
}
