package com.akole.dividox.common.ui.resources.components.connectivity

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Displays network connectivity status banner at the top of the screen.
 *
 * **States:**
 * - Offline (gray background): "No internet connection"
 * - Reconnecting (green background): "Connection restored"
 *
 * @param isOnline Whether device is online (true = connected, false = disconnected)
 * @param showReconnecting Whether to show the green "reconnecting" state
 */
@Composable
fun ConnectivityBanner(
    isOnline: Boolean,
    showReconnecting: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (isOnline && !showReconnecting) {
        return
    }

    val backgroundColor = if (showReconnecting) {
        // Green for reconnecting state
        MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.7f)
    } else {
        // Gray for offline state
        MaterialTheme.colorScheme.surfaceVariant
    }

    val message = if (showReconnecting) {
        "Connection restored"
    } else {
        "No internet connection"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(backgroundColor)
            .animateContentSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}
