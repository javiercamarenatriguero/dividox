package com.akole.dividox.feature.portfolio

import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.usecase.AddHoldingUseCase
import com.akole.dividox.component.portfolio.domain.usecase.RemoveHoldingUseCase
import com.akole.dividox.component.portfolio.domain.usecase.UpdateHoldingUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/** User-facing outcome of a position change, rendered as an app-wide snackbar. */
sealed interface PositionFeedback {
    val ticker: String

    data class Added(override val ticker: String, val holdingId: HoldingId) : PositionFeedback
    data class Updated(override val ticker: String) : PositionFeedback
    data class Removed(override val ticker: String) : PositionFeedback
    data class Failed(override val ticker: String) : PositionFeedback
}

/**
 * Single entry point for writing positions (add / update / remove / undo).
 *
 * The form navigates back optimistically, so writes run in an application-wide [scope] instead of
 * the form's `viewModelScope` (which is cancelled as soon as the screen is popped). Every outcome is
 * published on [feedback] so the app shell can confirm it and offer "Undo".
 *
 * Intended to be a DI singleton.
 */
class PositionActions(
    private val addHolding: AddHoldingUseCase,
    private val updateHolding: UpdateHoldingUseCase,
    private val removeHolding: RemoveHoldingUseCase,
    private val scope: CoroutineScope,
) {
    private val _feedback = MutableSharedFlow<PositionFeedback>(extraBufferCapacity = FEEDBACK_BUFFER)
    val feedback: SharedFlow<PositionFeedback> = _feedback.asSharedFlow()

    fun add(holding: Holding) {
        scope.launch {
            addHolding.execute(holding)
                .onSuccess { id -> _feedback.emit(PositionFeedback.Added(holding.tickerId, id)) }
                .onFailure { _feedback.emit(PositionFeedback.Failed(holding.tickerId)) }
        }
    }

    fun update(holding: Holding) {
        scope.launch {
            updateHolding.execute(holding)
                .onSuccess { _feedback.emit(PositionFeedback.Updated(holding.tickerId)) }
                .onFailure { _feedback.emit(PositionFeedback.Failed(holding.tickerId)) }
        }
    }

    fun remove(holdingId: HoldingId, ticker: String) {
        scope.launch {
            removeHolding.execute(holdingId)
                .onSuccess { _feedback.emit(PositionFeedback.Removed(ticker)) }
                .onFailure { _feedback.emit(PositionFeedback.Failed(ticker)) }
        }
    }

    /** Reverts a just-added position (snackbar "Undo"). */
    fun undo(added: PositionFeedback.Added) {
        scope.launch { removeHolding.execute(added.holdingId) }
    }

    private companion object {
        const val FEEDBACK_BUFFER = 4
    }
}
