package com.akole.dividox.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akole.dividox.component.market.domain.model.MarketIndexQuote
import com.akole.dividox.common.ui.resources.theme.extendedColors
import com.akole.dividox.common.ui.resources.theme.spacing
import org.jetbrains.compose.resources.stringResource
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.dashboard_market_indices_title
import dividox.common.ui_resources.generated.resources.dashboard_market_indices_error

private val CARD_WIDTH = 152.dp
private val CARD_HEIGHT = 108.dp

private fun marketFlag(marketKey: String) = when (marketKey) {
    "US" -> "🇺🇸"
    "ES" -> "🇪🇸"
    "DE" -> "🇩🇪"
    "JP" -> "🇯🇵"
    "UK" -> "🇬🇧"
    "EU" -> "🇪🇺"
    else -> "🌍"
}

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
        items(4) {
            Box(
                modifier = Modifier
                    .width(CARD_WIDTH)
                    .height(CARD_HEIGHT)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                    ),
            )
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
    val isPositive = index.changePercent >= 0
    val changeColor = if (isPositive) MaterialTheme.extendedColors.profit
    else MaterialTheme.colorScheme.error
    val changeSign = if (isPositive) "+" else ""

    ElevatedCard(
        modifier = Modifier
            .width(CARD_WIDTH)
            .height(CARD_HEIGHT),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Header: flag + name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = marketFlag(index.marketKey),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = index.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Points (large) + "pts" (small)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = String.format("%.0f", index.points),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = "pts",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }

            // Percent + delta stacked
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "$changeSign${String.format("%.2f", index.changePercent)}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = changeColor,
                )
                Text(
                    text = "$changeSign${String.format("%.0f", index.changePoints)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = changeColor,
                )
            }
        }
    }
}
