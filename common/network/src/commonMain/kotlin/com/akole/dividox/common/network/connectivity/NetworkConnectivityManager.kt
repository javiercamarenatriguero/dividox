package com.akole.dividox.common.network.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic network connectivity detection manager.
 *
 * Emits connectivity state changes via a reactive Flow.
 * Implementations handle platform-specific network detection:
 * - Android: ConnectivityManager.registerNetworkCallback()
 * - iOS: Network.framework (NWPathMonitor)
 * - Desktop (JVM): Polling InetAddress.isReachable()
 *
 * **Emissions:**
 * - `true`: Device has active internet connection
 * - `false`: Device is offline or connection lost
 *
 * **Behavior:**
 * - Emits initial state immediately on subscription
 * - Debounced 500ms to prevent rapid flapping from weak signal fluctuations
 * - Continues emitting state changes until Flow is canceled
 *
 * **Thread Safety:**
 * - Safe to call from any coroutine context
 * - Flow collection respects coroutine cancellation
 */
expect class NetworkConnectivityManager {
    /**
     * Observes network connectivity state changes.
     *
     * @return Flow<Boolean> that emits connectivity state:
     *         true = connected, false = disconnected
     *         Emits initial state immediately on subscription.
     */
    fun observeConnectivity(): Flow<Boolean>
}
