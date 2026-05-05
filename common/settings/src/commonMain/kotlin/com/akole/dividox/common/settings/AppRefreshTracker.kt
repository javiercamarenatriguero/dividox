package com.akole.dividox.common.settings

import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide singleton that tracks the last time any screen successfully refreshed its data.
 * Both Dashboard and Dividend Activity write to it on successful data load,
 * and both read from it to display a consistent "Updated at HH:mm" timestamp.
 */
class AppRefreshTracker {
    private val _lastRefreshed = MutableStateFlow<Instant?>(null)
    val lastRefreshed: StateFlow<Instant?> = _lastRefreshed.asStateFlow()

    fun notifyRefreshed(instant: Instant) {
        _lastRefreshed.value = instant
    }
}
