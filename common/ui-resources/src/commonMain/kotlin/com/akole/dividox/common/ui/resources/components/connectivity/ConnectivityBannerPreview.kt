package com.akole.dividox.common.ui.resources.components.connectivity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.akole.dividox.common.ui.resources.theme.DividoxTheme

/**
 * Preview composables for ConnectivityBanner states.
 *
 * Shows:
 * - Offline state (gray banner, "No internet connection")
 * - Reconnecting state (green banner, "Connection restored")
 * - Both light and dark themes
 */

@Preview(name = "Offline Banner - Light", showBackground = true)
@Composable
fun PreviewConnectivityBannerOfflineLight() {
    DividoxTheme(darkTheme = false) {
        Surface {
            Column(modifier = Modifier.fillMaxWidth()) {
                ConnectivityBanner(
                    isOnline = false,
                    showReconnecting = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(name = "Offline Banner - Dark", showBackground = true)
@Composable
fun PreviewConnectivityBannerOfflineDark() {
    DividoxTheme(darkTheme = true) {
        Surface {
            Column(modifier = Modifier.fillMaxWidth()) {
                ConnectivityBanner(
                    isOnline = false,
                    showReconnecting = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(name = "Reconnecting Banner - Light", showBackground = true)
@Composable
fun PreviewConnectivityBannerReconnectingLight() {
    DividoxTheme(darkTheme = false) {
        Surface {
            Column(modifier = Modifier.fillMaxWidth()) {
                ConnectivityBanner(
                    isOnline = true,
                    showReconnecting = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(name = "Reconnecting Banner - Dark", showBackground = true)
@Composable
fun PreviewConnectivityBannerReconnectingDark() {
    DividoxTheme(darkTheme = true) {
        Surface {
            Column(modifier = Modifier.fillMaxWidth()) {
                ConnectivityBanner(
                    isOnline = true,
                    showReconnecting = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(name = "Online Banner (Hidden) - Light", showBackground = true)
@Composable
fun PreviewConnectivityBannerOnlineLight() {
    DividoxTheme(darkTheme = false) {
        Surface {
            Column(modifier = Modifier.fillMaxWidth()) {
                ConnectivityBanner(
                    isOnline = true,
                    showReconnecting = false,
                    modifier = Modifier.fillMaxWidth(),
                )
                // Note: Banner is hidden when online and not reconnecting
            }
        }
    }
}
