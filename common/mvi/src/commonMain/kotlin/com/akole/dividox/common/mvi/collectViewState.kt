package com.akole.dividox.common.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow

/**
 * Collects a [StateFlow] as Compose [State].
 *
 * Uses [collectAsState] which is available across all KMP targets
 * (Android, iOS, Desktop) without lifecycle dependency.
 */
@Composable
fun <VS> collectViewState(viewState: StateFlow<VS>): State<VS> =
    viewState.collectAsState()
