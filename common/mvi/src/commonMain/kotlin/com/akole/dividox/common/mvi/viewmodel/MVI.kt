package com.akole.dividox.common.mvi.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Core MVI interface that drives unidirectional data flow.
 *
 * @param VS ViewState — immutable snapshot of the UI state.
 * @param VE ViewEvent — user actions or UI-triggered events.
 * @param SE SideEffect — one-off effects (navigation, toasts, etc.).
 */
interface MVI<VS, VE, SE> {
    val viewState: StateFlow<VS>
    val sideEffect: Flow<SE>

    fun onViewEvent(viewEvent: VE) {}

    fun updateViewState(block: VS.() -> VS)

    fun updateViewState(newViewState: VS)

    fun CoroutineScope.emitSideEffect(effect: SE)
}
