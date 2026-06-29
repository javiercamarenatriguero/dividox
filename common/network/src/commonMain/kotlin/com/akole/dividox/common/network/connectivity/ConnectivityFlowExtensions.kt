package com.akole.dividox.common.network.connectivity

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import kotlin.time.Duration

/**
 * Delays only offline (`false`) emissions by [timeout] while passing online (`true`)
 * emissions through immediately. If connectivity is restored within the timeout window,
 * the transient offline emission is suppressed entirely.
 *
 * Includes [distinctUntilChanged] to suppress duplicate consecutive values.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<Boolean>.debounceOfflineOnly(timeout: Duration): Flow<Boolean> = transformLatest { isOnline ->
    if (isOnline) {
        emit(true)
    } else {
        delay(timeout)
        emit(false)
    }
}.distinctUntilChanged()
