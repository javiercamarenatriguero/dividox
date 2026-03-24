package com.akole.dividox.common.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Collects a [sideEffect] flow and invokes [onSideEffect] for each emission.
 *
 * Uses [LaunchedEffect] scoped to the flow instance — cancelled and restarted
 * whenever [sideEffect] changes (e.g. on recomposition with a new flow).
 */
@Composable
fun <SE> CollectSideEffect(
    sideEffect: Flow<SE>,
    onSideEffect: suspend CoroutineScope.(effect: SE) -> Unit,
) {
    LaunchedEffect(sideEffect) {
        sideEffect.collect { onSideEffect(it) }
    }
}
