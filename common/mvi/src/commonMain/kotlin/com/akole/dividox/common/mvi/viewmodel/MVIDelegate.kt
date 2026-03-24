package com.akole.dividox.common.mvi.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MVIDelegate<VS, VE, SE> internal constructor(
    initialViewState: VS,
) : MVI<VS, VE, SE> {

    private val _viewState = MutableStateFlow(initialViewState)
    override val viewState: StateFlow<VS> = _viewState.asStateFlow()

    private val _sideEffect by lazy { Channel<SE>() }
    override val sideEffect: Flow<SE> by lazy {
        _sideEffect.receiveAsFlow().conflate()
    }

    override fun updateViewState(newViewState: VS) {
        _viewState.update { newViewState }
    }

    override fun updateViewState(block: VS.() -> VS) {
        _viewState.update(block)
    }

    override fun CoroutineScope.emitSideEffect(effect: SE) {
        launch { _sideEffect.send(effect) }
    }
}

/**
 * Creates an [MVI] delegate with the given [initialViewState].
 *
 * Usage in ViewModel:
 * ```kotlin
 * class MyViewModel : ViewModel(),
 *     MVI<MyViewState, MyViewEvent, MySideEffect> by mvi(MyViewState()) {
 *
 *     override fun onViewEvent(viewEvent: MyViewEvent) { ... }
 * }
 * ```
 */
fun <VS, VE, SE> mvi(initialViewState: VS): MVI<VS, VE, SE> = MVIDelegate(initialViewState)
