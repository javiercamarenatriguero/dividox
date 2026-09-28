package com.akole.dividox.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.dashboard_empty_message
import dividox.common.ui_resources.generated.resources.dashboard_empty_title
import dividox.common.ui_resources.generated.resources.portfolio_empty_cta
import org.jetbrains.compose.resources.stringResource

private const val EMPTY_CARD_CORNER_DP = 24

/** Replaces the portfolio overview when the user has no positions yet. */
@Composable
internal fun EmptyPortfolioCard(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(EMPTY_CARD_CORNER_DP.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.padding(MaterialTheme.spacing.medium).size(MaterialTheme.spacing.xLarge),
                )
            }
            Text(
                text = stringResource(Res.string.dashboard_empty_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.dashboard_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onAddClick, modifier = Modifier.padding(top = MaterialTheme.spacing.small)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(MaterialTheme.spacing.iconSmall))
                Spacer(Modifier.width(MaterialTheme.spacing.small))
                Text(stringResource(Res.string.portfolio_empty_cta))
            }
        }
    }
}

@Preview
@Composable
private fun EmptyPortfolioCardPreview() {
    DividoxTheme { EmptyPortfolioCard(onAddClick = {}) }
}
