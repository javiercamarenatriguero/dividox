package com.akole.dividox.common.ui.resources.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.ui.resources.theme.spacing

@Composable
fun MarketFilterRow(
    selectedMarket: ExchangeMarket,
    onMarketSelected: (ExchangeMarket) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = MaterialTheme.spacing.medium),
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        items(ExchangeMarket.entries, key = { it.name }) { market ->
            FilterChip(
                selected = selectedMarket == market,
                onClick = { onMarketSelected(market) },
                label = {
                    Text(
                        text = "${market.emoji} ${market.label}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
            )
        }
    }
}
