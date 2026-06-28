# Task: DVX-TK-044 — Fix Connectivity Banner False Positive on App Resume

## Problem

The "Disconnected" banner sometimes appears when the app returns from background/sleep,
even though the device has a working internet connection. After a moment the banner
transitions to "Reconnected" and dismisses — but the user was never actually offline.

## Root Cause Analysis

The connectivity detection chain:

1. **Platform `NetworkConnectivityManager`** — emits `Flow<Boolean>` for online/offline
2. **`ConnectivityBannerHost`** — state machine that shows/hides banner based on flow

Likely causes:
- **Android**: `ConnectivityManager` callback fires `onLost` when the app resumes from doze/standby,
  then immediately fires `onAvailable`. The brief `false` emission triggers the banner.
- **iOS**: `nw_path_monitor` may report `unsatisfied` briefly during app activation.
- **Desktop/JVM**: `InetAddress.isReachable()` may timeout on first check after sleep.

## Affected Files

- `common/network/src/androidMain/.../NetworkConnectivityManager.kt`
- `common/network/src/iosMain/.../NetworkConnectivityManager.kt`
- `common/network/src/jvmMain/.../NetworkConnectivityManager.kt`
- `common/ui-resources/.../connectivity/ConnectivityBannerHost.kt`

## Possible Fixes

### Option A — Debounce at the Flow level (recommended)
Add a short debounce (~500-1000ms) on `false` emissions only, so transient disconnects
during app resume don't trigger the banner:
```kotlin
connectivityFlow
    .debounceOfflineOnly(500.milliseconds)
    .collect { online -> ... }
```

### Option B — Debounce in ConnectivityBannerHost
Add a delay before showing the offline banner. If connectivity is restored within
the debounce window, skip showing the banner entirely.

### Option C — Lifecycle-aware suppression
Suppress connectivity changes for ~1 second after the app transitions from
`Lifecycle.State.STARTED` (background → foreground).

## Testing

- Put app in background, wait 30s+, bring back — banner should NOT flash
- Actually disconnect WiFi/data — banner SHOULD appear
- Reconnect — green "Reconnected" banner should appear and auto-dismiss

## Priority

**Medium** — cosmetic but erodes trust in app reliability.

## Labels

`bug`, `ux`, `network`
