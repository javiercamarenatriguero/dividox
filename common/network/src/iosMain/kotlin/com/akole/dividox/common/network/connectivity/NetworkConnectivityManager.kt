package com.akole.dividox.common.network.connectivity

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_get_status
import platform.darwin.dispatch_get_main_queue

/**
 * iOS implementation of [NetworkConnectivityManager].
 *
 * Uses Network.framework (NWPathMonitor) to monitor connectivity changes.
 * Available on iOS 12.0+. Emits connectivity state as Flow<Boolean> with 500ms debounce.
 *
 * **Platform Details:**
 * - Modern Network.framework approach (required for iOS 12+)
 * - Updates delivered on main dispatch queue
 * - Auto-detects initial state on subscription
 * - Properly cancels monitor on Flow cancellation
 */
actual class NetworkConnectivityManager {

    actual fun observeConnectivity(): Flow<Boolean> {
        return callbackFlow {
            val monitor = nw_path_monitor_create()

            nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())

            nw_path_monitor_set_update_handler(monitor) { path ->
                val isConnected = nw_path_get_status(path) == nw_path_status_satisfied
                trySend(isConnected)
            }

            nw_path_monitor_start(monitor)

            awaitClose {
                nw_path_monitor_cancel(monitor)
            }
        }
            .debounce(500)
            .distinctUntilChanged()
    }
}
