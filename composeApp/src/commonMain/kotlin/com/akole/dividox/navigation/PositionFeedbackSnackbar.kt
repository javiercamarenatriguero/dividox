package com.akole.dividox.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.akole.dividox.common.ui.resources.theme.spacing
import com.akole.dividox.feature.portfolio.PositionActions
import com.akole.dividox.feature.portfolio.PositionFeedback
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.action_undo
import dividox.common.ui_resources.generated.resources.action_view
import dividox.common.ui_resources.generated.resources.position_added
import dividox.common.ui_resources.generated.resources.position_removed
import dividox.common.ui_resources.generated.resources.position_save_failed
import dividox.common.ui_resources.generated.resources.position_updated
import org.jetbrains.compose.resources.getString

/** Snackbar payload for a position change; "View" and "Undo" are optional depending on the outcome. */
private class PositionSnackbarVisuals(
    override val message: String,
    val viewLabel: String?,
    val undoLabel: String?,
) : SnackbarVisuals {
    override val actionLabel: String? = undoLabel
    override val withDismissAction: Boolean = false
    override val duration: SnackbarDuration = SnackbarDuration.Short
}

/**
 * Turns every [PositionFeedback] into a snackbar on [hostState]. "Undo" (only for a just-added
 * position) reverts it through [positionActions].
 */
@Composable
fun CollectPositionFeedback(positionActions: PositionActions, hostState: SnackbarHostState) {
    LaunchedEffect(positionActions, hostState) {
        positionActions.feedback.collect { feedback ->
            hostState.currentSnackbarData?.dismiss()
            val result = hostState.showSnackbar(feedback.toVisuals())
            if (result == SnackbarResult.ActionPerformed && feedback is PositionFeedback.Added) {
                positionActions.undo(feedback)
            }
        }
    }
}

private suspend fun PositionFeedback.toVisuals(): PositionSnackbarVisuals = when (this) {
    is PositionFeedback.Added -> PositionSnackbarVisuals(
        message = getString(Res.string.position_added, ticker),
        viewLabel = getString(Res.string.action_view),
        undoLabel = getString(Res.string.action_undo),
    )
    is PositionFeedback.Updated -> PositionSnackbarVisuals(
        message = getString(Res.string.position_updated, ticker),
        viewLabel = getString(Res.string.action_view),
        undoLabel = null,
    )
    is PositionFeedback.Removed -> PositionSnackbarVisuals(
        message = getString(Res.string.position_removed, ticker),
        viewLabel = null,
        undoLabel = null,
    )
    is PositionFeedback.Failed -> PositionSnackbarVisuals(
        message = getString(Res.string.position_save_failed, ticker),
        viewLabel = null,
        undoLabel = null,
    )
}

/** Snackbar host able to render the two-action ("View" · "Undo") position snackbar. */
@Composable
fun PositionSnackbarHost(
    hostState: SnackbarHostState,
    onViewPortfolio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Breathing room from the screen edges and from the FAB / bottom bar it sits on.
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(
            start = MaterialTheme.spacing.medium,
            end = MaterialTheme.spacing.medium,
            bottom = MaterialTheme.spacing.medium,
        ),
    ) { data ->
        val visuals = data.visuals as? PositionSnackbarVisuals
        if (visuals == null) Snackbar(snackbarData = data) else PositionSnackbar(data, visuals, onViewPortfolio)
    }
}

@Composable
private fun PositionSnackbar(
    data: SnackbarData,
    visuals: PositionSnackbarVisuals,
    onViewPortfolio: () -> Unit,
) {
    val actionColor = MaterialTheme.colorScheme.inversePrimary
    Snackbar(
        action = {
            Row {
                visuals.viewLabel?.let { label ->
                    TextButton(onClick = {
                        data.dismiss()
                        onViewPortfolio()
                    }) { Text(label, color = actionColor) }
                }
                visuals.undoLabel?.let { label ->
                    TextButton(onClick = data::performAction) { Text(label, color = actionColor) }
                }
            }
        },
    ) {
        Text(visuals.message)
    }
}
