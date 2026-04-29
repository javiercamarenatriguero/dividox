package com.akole.dividox.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.ui.resources.format.formatPercent
import com.akole.dividox.common.ui.resources.format.formatPercentSigned
import com.akole.dividox.common.ui.resources.format.formatPrice
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardSideEffect
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewState
import com.akole.dividox.integration.security.domain.model.EnrichedWatchlistEntry
import com.akole.dividox.integration.security.domain.model.PortfolioSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun DashboardScreen(
    state: DashboardViewState,
    onEvent: (DashboardViewEvent) -> Unit,
    sideEffects: Flow<DashboardSideEffect>,
    onNavigation: (DashboardSideEffect.Navigation) -> Unit,
) {
    CollectSideEffect(sideEffects) { effect ->
        when (effect) {
            is DashboardSideEffect.Navigation -> onNavigation(effect)
        }
    }

    DashboardContent(
        state = state,
        onEvent = onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    state: DashboardViewState,
    onEvent: (DashboardViewEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.section_dashboard),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                actions = {
                    CurrencyToggleButton(
                        showInEur = state.showInEur,
                        onClick = { onEvent(DashboardViewEvent.CurrencyToggled) },
                    )
                },
            )
        },
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = MaterialTheme.spacing.medium),
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                PeriodSelectorRow(
                    selectedPeriod = state.selectedPeriod,
                    onPeriodSelected = { onEvent(DashboardViewEvent.PeriodSelected(it)) },
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                MetricsBlock(
                    summary = state.summary,
                    showInEur = state.showInEur,
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                FavouritesSection(
                    watchlist = state.watchlist,
                    onFavouriteToggled = { ticker ->
                        onEvent(DashboardViewEvent.FavouriteToggled(ticker))
                    },
                    onSecurityClicked = { ticker ->
                        onEvent(DashboardViewEvent.SecurityClicked(ticker))
                    },
                    onViewAllClicked = { onEvent(DashboardViewEvent.ViewAllFavouritesClicked) },
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                DisclaimerText()

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            }
        }
    }
}

// ─── Currency toggle ──────────────────────────────────────────────────────────

@Composable
private fun CurrencyToggleButton(
    showInEur: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Text(
            text = if (showInEur) stringResource(Res.string.currency_eur) else stringResource(Res.string.currency_usd),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ─── Period selector ──────────────────────────────────────────────────────────

@Composable
private fun PeriodSelectorRow(
    selectedPeriod: ChartPeriod,
    onPeriodSelected: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ChartPeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPeriodSelected(period) },
            ) {
                Text(
                    text = period.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }
    }
}

// ─── Metric cards ─────────────────────────────────────────────────────────────

@Composable
private fun MetricsBlock(
    summary: PortfolioSummary?,
    showInEur: Boolean,
    modifier: Modifier = Modifier,
) {
    val currencyCode = if (showInEur) "EUR" else "USD"
    val isEmpty = summary == null || summary.totalValue == 0.0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            MetricCard(
                label = stringResource(Res.string.metric_total_value),
                value = (summary?.totalValue ?: 0.0).formatPrice(currencyCode),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = stringResource(Res.string.metric_total_gain),
                value = (summary?.totalGainPercent ?: 0.0).formatPercent(),
                valueColor = if (!isEmpty && summary!!.totalGainPercent >= 0) {
                    Color(0xFF2E7D32)
                } else if (!isEmpty) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            MetricCard(
                label = stringResource(Res.string.metric_yield),
                value = (summary?.totalYield ?: 0.0).formatPercent(),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = stringResource(Res.string.metric_dividends),
                value = (summary?.dividendsCollected ?: 0.0).formatPrice(currencyCode),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
            )
        }
    }
}

// ─── Favourites section ───────────────────────────────────────────────────────

@Composable
private fun FavouritesSection(
    watchlist: List<EnrichedWatchlistEntry>,
    onFavouriteToggled: (String) -> Unit,
    onSecurityClicked: (String) -> Unit,
    onViewAllClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.section_favourites),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (watchlist.isNotEmpty()) {
                TextButton(onClick = onViewAllClicked) {
                    Text(
                        text = stringResource(Res.string.action_view_all),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        if (watchlist.isEmpty()) {
            Text(
                text = stringResource(Res.string.favourites_empty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = MaterialTheme.spacing.small),
            )
        } else {
            watchlist.take(2).forEach { entry ->
                WatchlistEntryRow(
                    entry = entry,
                    onFavouriteToggled = onFavouriteToggled,
                    onSecurityClicked = onSecurityClicked,
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))
            }
        }
    }
}

@Composable
private fun WatchlistEntryRow(
    entry: EnrichedWatchlistEntry,
    onFavouriteToggled: (String) -> Unit,
    onSecurityClicked: (String) -> Unit,
) {
    val ticker = entry.entry.tickerId
    val price = entry.quote?.price
    val changePercent = entry.quote?.changePercent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSecurityClicked(ticker) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ticker,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                val name = entry.companyInfo?.name
                if (name != null) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (price != null) {
                val currency = entry.quote?.currency ?: "USD"
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = price.formatPrice(currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    if (changePercent != null) {
                        Text(
                            text = changePercent.formatPercentSigned(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (changePercent >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            IconButton(
                onClick = { onFavouriteToggled(ticker) },
                modifier = Modifier.size(MaterialTheme.spacing.iconMedium + 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = stringResource(Res.string.cd_remove_from_favourites),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
                )
            }
        }
    }
}

// ─── Disclaimer ───────────────────────────────────────────────────────────────

@Composable
private fun DisclaimerText(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.disclaimer_prices_delayed),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun DashboardScreenLoadingPreview() {
    DividoxTheme {
        DashboardContent(
            state = DashboardViewState(),
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun DashboardScreenEmptyPreview() {
    DividoxTheme {
        DashboardContent(
            state = DashboardViewState(
                isLoading = false,
                summary = PortfolioSummary(
                    totalValue = 0.0,
                    totalGain = 0.0,
                    totalGainPercent = 0.0,
                    totalYield = 0.0,
                    dividendsCollected = 0.0,
                ),
            ),
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun DashboardScreenWithDataPreview() {
    DividoxTheme {
        DashboardContent(
            state = DashboardViewState(
                isLoading = false,
                summary = PortfolioSummary(
                    totalValue = 24_350.00,
                    totalGain = 1_200.50,
                    totalGainPercent = 5.19,
                    totalYield = 3.24,
                    dividendsCollected = 788.40,
                ),
                selectedPeriod = ChartPeriod.ONE_MONTH,
                showInEur = false,
            ),
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun DashboardScreenDarkPreview() {
    DividoxTheme(darkTheme = true) {
        DashboardContent(
            state = DashboardViewState(
                isLoading = false,
                summary = PortfolioSummary(
                    totalValue = 24_350.00,
                    totalGain = 1_200.50,
                    totalGainPercent = 5.19,
                    totalYield = 3.24,
                    dividendsCollected = 788.40,
                ),
                selectedPeriod = ChartPeriod.ONE_MONTH,
            ),
            onEvent = {},
        )
    }
}
