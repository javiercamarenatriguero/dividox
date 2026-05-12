package com.akole.dividox.common.ui.resources.components.connectivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
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
    var isOnline by retain { mutableStateOf(true) }
    var showReconnecting by retain { mutableStateOf(false) }
    var previousOnline by retain { mutableStateOf(true) }

    LaunchedEffect(connectivityFlow) {
        connectivityFlow.collect { online ->
            when {
                !online -> {
                    isOnline = false
                    showReconnecting = false
                }
                online && !previousOnline -> {
                    isOnline = true
                    showReconnecting = true
                }
                else -> {
                    isOnline = true
                }
            }
            previousOnline = online
        }
    }

    // Separate effect so the collect lambda is never blocked by the dismiss delay
    LaunchedEffect(showReconnecting) {
        if (showReconnecting) {
            delay(2500)
            showReconnecting = false
        }
    }

    ConnectivityBanner(
        isOnline = isOnline,
        showReconnecting = showReconnecting,
    )
}
