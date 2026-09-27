package com.akole.dividox.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.action_view_all
import org.jetbrains.compose.resources.stringResource

/**
 * Compact section header ("Your Portfolio Today", "Favourites", …) with an optional "View all".
 *
 * The default M3 [TextButton] reserves a 48dp touch target, which made every header ~48dp tall and
 * pushed the content far below the title. Here the button is capped at [MaterialTheme.spacing.xLarge]
 * so headers stay visually tight; the row keeps that height even without the button so all
 * sections align.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onViewAllClicked: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MaterialTheme.spacing.xLarge),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (onViewAllClicked != null) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                TextButton(
                    onClick = onViewAllClicked,
                    modifier = Modifier.heightIn(min = MaterialTheme.spacing.xLarge),
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.small),
                ) {
                    Text(
                        text = stringResource(Res.string.action_view_all),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
