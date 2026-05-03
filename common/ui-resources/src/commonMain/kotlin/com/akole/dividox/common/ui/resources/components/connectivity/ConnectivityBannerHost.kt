package com.akole.dividox.common.ui.resources.components.connectivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * Stateful host composable that manages connectivity banner display logic.
 *
 * **State Machine:**
 * - Offline: Gray banner persists until connectivity restored
 * - Reconnecting: Green banner shows for 2.5s then auto-dismisses
 *
 * Listens to a connectivity Flow and manages the banner visibility and state transitions.
 *
 * @param connectivityFlow Flow<Boolean> where true = online, false = offline
 */
@Composable
fun ConnectivityBannerHost(connectivityFlow: Flow<Boolean>) {
    val isOnline = remember { mutableStateOf(true) }
    val showReconnecting = remember { mutableStateOf(false) }

    LaunchedEffect(connectivityFlow) {
        connectivityFlow.collect { online ->
            when {
                online && !isOnline.value -> {
                    // Transitioned from offline to online
                    isOnline.value = true
                    showReconnecting.value = true

                    // Auto-dismiss green banner after 2.5s
                    delay(2500)
                    showReconnecting.value = false
                }

                !online -> {
                    // Went offline
                    isOnline.value = false
                    showReconnecting.value = false
                }

                else -> {
                    // Already online or no state change
                    isOnline.value = online
                }
            }
        }
    }

    ConnectivityBanner(
        isOnline = isOnline.value,
        showReconnecting = showReconnecting.value,
    )
}
