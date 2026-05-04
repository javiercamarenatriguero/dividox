package com.akole.dividox.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.ui.resources.components.AnimatedValueText
import com.akole.dividox.common.ui.resources.components.DividoxPullToRefreshBox
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.LastUpdatedBar
import com.akole.dividox.common.ui.resources.components.connectivity.ConnectivityBannerHost
import com.akole.dividox.common.ui.resources.components.connectivity.LocalNetworkConnectivityManager
import com.akole.dividox.common.ui.resources.format.formatPercent
import com.akole.dividox.common.ui.resources.format.formatPercentSigned
import com.akole.dividox.common.ui.resources.format.formatPrice
import com.akole.dividox.common.ui.resources.format.formatPriceSigned
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.common.ui.resources.theme.extendedColors
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardSideEffect
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewState
import com.akole.dividox.integration.security.domain.model.EnrichedWatchlistEntry
import com.akole.dividox.integration.security.domain.model.PortfolioSummary
import dividox.common.ui_resources.generated.resources.*
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

@Composable
private fun DashboardContent(
    state: DashboardViewState,
    onEvent: (DashboardViewEvent) -> Unit,
) {
    val connectivityManager = LocalNetworkConnectivityManager.current

    Scaffold(
        topBar = {
            DividoxTopAppBar(
                title = stringResource(Res.string.section_dashboard),
                actions = {
                    CurrencyDropdown(
                        selected = state.currency,
                        onCurrencySelected = { onEvent(DashboardViewEvent.CurrencySelected(it)) },
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ConnectivityBannerHost(connectivityFlow = connectivityManager.observeConnectivity())
            LastUpdatedBar(
                lastUpdated = state.lastUpdated,
                onRefresh = { onEvent(DashboardViewEvent.Refresh) },
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                DividoxPullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onEvent(DashboardViewEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = MaterialTheme.spacing.medium),
                ) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                PeriodSelectorRow(
                    selectedPeriod = state.selectedPeriod,
                    onPeriodSelected = { onEvent(DashboardViewEvent.PeriodSelected(it)) },
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                MetricsBlock(
                    summary = state.convertedSummary ?: state.summary,
                    currency = state.currency,
                    selectedPeriod = state.selectedPeriod,
                    periodGainPercent = state.periodGainPercent,
                    periodGainAbsolute = state.periodGainAbsolute,
                    periodDividends = state.periodDividends,
                    lifetimeDividends = state.lifetimeDividends,
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                FavouritesSection(
                    watchlist = state.watchlist,
                    convertedPrices = state.convertedWatchlistPrices,
                    displayCurrency = state.currency,
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
    }
}

// ─── Currency dropdown ────────────────────────────────────────────────────────

private val PINNED_CURRENCIES = listOf(Currency.EUR, Currency.USD, Currency.GBP)
private val CURRENCY_LIST: List<Currency> = PINNED_CURRENCIES +
    Currency.entries.filter { it !in PINNED_CURRENCIES }.sortedBy { it.code }

@Composable
private fun CurrencyDropdown(
    selected: Currency,
    onCurrencySelected: (Currency) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.padding(end = MaterialTheme.spacing.small)) {
        FilledTonalButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        ) {
            Text(
                text = "${selected.symbol.trim()} ${selected.code}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            CURRENCY_LIST.forEach { currency ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${currency.symbol.trim()} ${currency.code}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (currency == selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (currency == selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    },
                    onClick = {
                        expanded = false
                        onCurrencySelected(currency)
                    },
                )
            }
        }
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
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.xSmall),
                )
            }
        }
    }
}

// ─── Metric cards ─────────────────────────────────────────────────────────────

@Composable
private fun MetricsBlock(
    summary: PortfolioSummary?,
    currency: Currency,
    selectedPeriod: ChartPeriod,
    periodGainPercent: Double,
    periodGainAbsolute: Double,
    periodDividends: Double,
    lifetimeDividends: Double,
    modifier: Modifier = Modifier,
) {
    val isEmpty = summary == null || summary.totalValue == 0.0
    val gainColor = when {
        isEmpty -> MaterialTheme.colorScheme.onSurfaceVariant
        periodGainAbsolute >= 0 -> MaterialTheme.extendedColors.profit
        else -> MaterialTheme.colorScheme.error
    }
    val totalValue = (summary?.totalValue ?: 0.0).formatPrice(currency)
    val invested = ((summary?.totalValue ?: 0.0) - (summary?.totalGain ?: 0.0)).formatPrice(currency)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        PortfolioHeroCard(
            totalValue = totalValue,
            invested = invested,
            gainAbsolute = periodGainAbsolute.formatPriceSigned(currency),
            gainPercent = periodGainPercent.formatPercentSigned(),
            gainColor = gainColor,
            periodLabel = selectedPeriod.label,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            YieldChip(
                yield = (summary?.totalYield ?: 0.0).formatPercent(),
                modifier = Modifier.weight(1f),
            )
            DividendsChip(
                lifetimeDividends = lifetimeDividends.formatPrice(currency),
                periodDividends = periodDividends.formatPrice(currency),
                periodLabel = selectedPeriod.label,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PortfolioHeroCard(
    totalValue: String,
    invested: String,
    gainAbsolute: String,
    gainPercent: String,
    gainColor: Color,
    periodLabel: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.large),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.metric_total_value),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AnimatedValueText(
                        value = totalValue,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        autoShrink = true,
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.small)
                        .width(1.dp)
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.metric_invested),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AnimatedValueText(
                        value = invested,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        autoShrink = true,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gainColor.copy(alpha = 0.10f))
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
                ) {
                    AnimatedValueText(
                        value = gainAbsolute,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = gainColor,
                        autoShrink = true,
                    )
                    AnimatedValueText(
                        value = "($gainPercent)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                        color = gainColor,
                    )
                }
                Text(
                    text = periodLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun YieldChip(
    yield: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Text(
                text = stringResource(Res.string.metric_yield),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedValueText(
                value = yield,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                autoShrink = true,
            )
        }
    }
}

@Composable
private fun DividendsChip(
    lifetimeDividends: String,
    periodDividends: String,
    periodLabel: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Text(
                text = stringResource(Res.string.metric_dividends),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedValueText(
                value = lifetimeDividends,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                autoShrink = true,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                AnimatedValueText(
                    value = periodDividends,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "· $periodLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─── Favourites section ───────────────────────────────────────────────────────

@Composable
private fun FavouritesSection(
    watchlist: List<EnrichedWatchlistEntry>,
    convertedPrices: Map<String, Double>,
    displayCurrency: Currency,
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
                    convertedPrice = convertedPrices[entry.entry.tickerId],
                    displayCurrency = displayCurrency,
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
    convertedPrice: Double?,
    displayCurrency: Currency,
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
                val displayPrice = convertedPrice ?: price
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedValueText(
                        value = displayPrice.formatPrice(displayCurrency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    if (changePercent != null) {
                        Text(
                            text = changePercent.formatPercentSigned(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (changePercent >= 0) MaterialTheme.extendedColors.profit else MaterialTheme.colorScheme.error,
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
                currency = Currency.USD,
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
