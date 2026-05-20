package com.akole.dividox.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akole.dividox.component.market.domain.model.MarketIndexQuote
import com.akole.dividox.common.ui.resources.theme.extendedColors
import com.akole.dividox.common.ui.resources.theme.spacing
import org.jetbrains.compose.resources.stringResource
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.dashboard_market_indices_title
import dividox.common.ui_resources.generated.resources.dashboard_market_indices_error

@Composable
fun MarketIndicesSection(
    indices: List<MarketIndexQuote>,
    isLoading: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.dashboard_market_indices_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        when {
            isLoading -> MarketIndicesLoadingPlaceholder()
            isError -> MarketIndicesError()
            else -> MarketIndicesCarousel(indices)
        }
    }
}

@Composable
private fun MarketIndicesLoadingPlaceholder() {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        items(3) {
            ElevatedCard(
                modifier = Modifier
                    .width(140.dp)
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                // Empty placeholder card
            }
        }
    }
}

@Composable
private fun MarketIndicesError() {
    Text(
        text = stringResource(Res.string.dashboard_market_indices_error),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun MarketIndicesCarousel(indices: List<MarketIndexQuote>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        items(indices, key = { it.ticker }) { index ->
            MarketIndexCard(index)
        }
    }
}

@Composable
private fun MarketIndexCard(index: MarketIndexQuote) {
    ElevatedCard(
        modifier = Modifier.width(140.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
        ) {
            Text(
                text = index.name,
                style = MaterialTheme.typography.labelSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
            )

            val changeColor = if (index.changePercent >= 0) {
                MaterialTheme.extendedColors.profit
            } else {
                MaterialTheme.colorScheme.error
            }
            val changeSign = if (index.changePercent >= 0) "+" else ""

            Text(
                text = "$changeSign${String.format("%.2f%%", index.changePercent)}",
                style = MaterialTheme.typography.titleSmall,
                color = changeColor,
            )

            Text(
                text = "${String.format("%.0f", index.points)} pts",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "$changeSign${String.format("%.0f", index.changePoints)} pts",
                style = MaterialTheme.typography.labelSmall,
                color = changeColor,
            )
        }
    }
}
