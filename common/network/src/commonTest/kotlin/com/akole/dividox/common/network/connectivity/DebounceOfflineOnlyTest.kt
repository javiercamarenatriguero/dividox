package com.akole.dividox.common.network.connectivity

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DebounceOfflineOnlyTest {

    private val timeout = 1.seconds

    @Test
    fun onlineEmissionPassesThroughImmediately() = runTest {
        val source = MutableSharedFlow<Boolean>()
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            source.debounceOfflineOnly(timeout).toList(results)
        }

        source.emit(true)
        runCurrent()
        assertEquals(listOf(true), results)
    }

    @Test
    fun offlineEmissionDelayedByTimeout() = runTest {
        val source = MutableSharedFlow<Boolean>()
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            source.debounceOfflineOnly(timeout).toList(results)
        }

        source.emit(false)
        runCurrent()
        assertEquals(emptyList(), results)

        advanceTimeBy(1001)
        runCurrent()
        assertEquals(listOf(false), results)
    }

    @Test
    fun transientOfflineSuppressedWhenOnlineArrivesDuringTimeout() = runTest {
        val source = MutableSharedFlow<Boolean>()
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            source.debounceOfflineOnly(timeout).toList(results)
        }

        // GIVEN online
        source.emit(true)
        runCurrent()
        assertEquals(listOf(true), results)

        // WHEN transient offline then quick reconnect
        source.emit(false)
        runCurrent()
        advanceTimeBy(500)
        source.emit(true)
        runCurrent()
        advanceTimeBy(600)
        runCurrent()

        // THEN no offline emission — only initial online
        assertEquals(listOf(true), results)
    }

    @Test
    fun realDisconnectEmitsAfterTimeout() = runTest {
        val source = MutableSharedFlow<Boolean>()
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            source.debounceOfflineOnly(timeout).toList(results)
        }

        source.emit(true)
        runCurrent()

        source.emit(false)
        runCurrent()
        advanceTimeBy(1001)
        runCurrent()

        assertEquals(listOf(true, false), results)
    }

    @Test
    fun duplicateConsecutiveValuesSuppressed() = runTest {
        val source = MutableSharedFlow<Boolean>()
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            source.debounceOfflineOnly(timeout).toList(results)
        }

        source.emit(true)
        runCurrent()
        source.emit(true)
        runCurrent()

        assertEquals(listOf(true), results)
    }

    @Test
    fun fullCycleDisconnectAndReconnect() = runTest {
        val source = MutableSharedFlow<Boolean>()
        val results = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            source.debounceOfflineOnly(timeout).toList(results)
        }

        // GIVEN online
        source.emit(true)
        runCurrent()

        // WHEN real disconnect
        source.emit(false)
        runCurrent()
        advanceTimeBy(1001)
        runCurrent()

        // THEN reconnect
        source.emit(true)
        runCurrent()

        assertEquals(listOf(true, false, true), results)
    }
}
