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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.ui.resources.components.AnimatedValueText
import com.akole.dividox.common.ui.resources.components.DividoxPullToRefreshBox
import com.akole.dividox.common.ui.resources.components.SecurityCard
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.LastUpdatedBar
import com.akole.dividox.common.ui.resources.components.connectivity.ConnectivityBannerHost
import com.akole.dividox.common.ui.resources.components.connectivity.LocalNetworkConnectivityManager
import com.akole.dividox.common.ui.resources.format.formatPercent
import com.akole.dividox.common.ui.resources.format.formatPercentSigned
import com.akole.dividox.common.ui.resources.format.formatPrice
import com.akole.dividox.common.ui.resources.format.formatPriceSigned
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.common.ui.resources.format.flag
import com.akole.dividox.common.ui.resources.format.nameRes
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

                MetricsBlock(
                    summary = state.convertedSummary ?: state.summary,
                    currency = state.currency,
                    totalGainPercent = state.totalGainPercent,
                    totalGainAbsolute = state.totalGainAbsolute,
                    lifetimeDividends = state.lifetimeDividends,
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                PeriodSelectorRow(
                    selectedPeriod = state.selectedPeriod,
                    onPeriodSelected = { onEvent(DashboardViewEvent.PeriodSelected(it)) },
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                PeriodDetailRow(
                    selectedPeriod = state.selectedPeriod,
                    periodGainPercent = state.periodGainPercent,
                    periodGainAbsolute = state.periodGainAbsolute,
                    periodDividends = state.periodDividends,
                    currency = state.currency,
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                if (state.topGainers.isNotEmpty() || state.topLosers.isNotEmpty()) {
                    PortfolioTodaySection(
                        topGainers = state.topGainers,
                        topLosers = state.topLosers,
                        onViewAllClicked = { onEvent(DashboardViewEvent.ViewAllPortfolioClicked) },
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                }

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
                text = "${selected.flag()} ${selected.symbol.trim()}",
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = currency.flag(),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Column {
                                Text(
                                    text = stringResource(currency.nameRes()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (currency == selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currency == selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                                Text(
                                    text = "${currency.symbol.trim()} ${currency.code}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
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
                    text = stringResource(period.labelRes()),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .padding(vertical = MaterialTheme.spacing.xSmall)
                        .fillMaxWidth(),
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
    totalGainPercent: Double,
    totalGainAbsolute: Double,
    lifetimeDividends: Double,
    modifier: Modifier = Modifier,
) {
    val isEmpty = summary == null || summary.totalValue == 0.0
    val gainColor = when {
        isEmpty -> MaterialTheme.colorScheme.onSurfaceVariant
        totalGainAbsolute >= 0 -> MaterialTheme.extendedColors.profit
        else -> MaterialTheme.colorScheme.error
    }
    val totalValue = (summary?.totalValue ?: 0.0).formatPrice(currency)
    val invested = ((summary?.totalValue ?: 0.0) - (summary?.totalGain ?: 0.0)).formatPrice(currency)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        PortfolioHeroCard(
            totalValue = totalValue,
            invested = invested,
            gainAbsolute = totalGainAbsolute.formatPriceSigned(currency),
            gainPercent = totalGainPercent.formatPercentSigned(),
            gainColor = gainColor,
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
        }
    }
}

// ─── Period detail ────────────────────────────────────────────────────────────

@Composable
private fun PeriodDetailRow(
    selectedPeriod: ChartPeriod,
    periodGainPercent: Double,
    periodGainAbsolute: Double,
    periodDividends: Double,
    currency: Currency,
    modifier: Modifier = Modifier,
) {
    val gainColor = when {
        periodGainAbsolute > 0 -> MaterialTheme.extendedColors.profit
        periodGainAbsolute < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                Text(
                    text = "${stringResource(Res.string.metric_period_gain)} (${stringResource(selectedPeriod.labelRes())})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedValueText(
                    value = periodGainPercent.formatPercentSigned(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = gainColor,
                    autoShrink = true,
                )
                AnimatedValueText(
                    value = periodGainAbsolute.formatPriceSigned(currency),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    color = gainColor,
                    autoShrink = true,
                )
            }
        }
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                Text(
                    text = "${stringResource(Res.string.metric_dividends)} (${stringResource(selectedPeriod.labelRes())})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedValueText(
                    value = periodDividends.formatPrice(currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    autoShrink = true,
                )
            }
        }
    }
}

// ─── Portfolio Today section ──────────────────────────────────────────────────

@Composable
private fun PortfolioTodaySection(
    topGainers: List<PortfolioTodayItem>,
    topLosers: List<PortfolioTodayItem>,
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
                text = stringResource(Res.string.dashboard_portfolio_today),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            TextButton(onClick = onViewAllClicked) {
                Text(
                    text = stringResource(Res.string.action_view_all),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            PortfolioTodayCard(
                isGainers = true,
                items = topGainers,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            PortfolioTodayCard(
                isGainers = false,
                items = topLosers,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun PortfolioTodayCard(
    isGainers: Boolean,
    items: List<PortfolioTodayItem>,
    modifier: Modifier = Modifier,
) {
    val accentColor = if (isGainers) MaterialTheme.extendedColors.profit else MaterialTheme.colorScheme.error
    val prefix = if (isGainers) "▲" else "▼"
    val titleRes = if (isGainers) Res.string.dashboard_gainers else Res.string.dashboard_losers

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Text(
                text = "$prefix ${stringResource(titleRes)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            if (items.isEmpty()) {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                items.forEachIndexed { index, item ->
                    PortfolioTodayRow(item = item, accentColor = accentColor)
                    if (index < items.lastIndex) {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    }
                }
            }
        }
    }
}

@Composable
private fun PortfolioTodayRow(
    item: PortfolioTodayItem,
    accentColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.ticker,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            if (item.name != null) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.changePercent.formatPercentSigned(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
            Text(
                text = item.price.formatPrice(item.currency),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
            watchlist.take(3).forEach { entry ->
                val ticker = entry.entry.tickerId
                SecurityCard(
                    ticker = ticker,
                    companyName = entry.companyInfo?.name,
                    price = convertedPrices[ticker] ?: entry.quote?.price,
                    changePercent = entry.quote?.changePercent,
                    currency = displayCurrency,
                    isFavorite = true,
                    isInPortfolio = entry.isInPortfolio,
                    onFavoriteToggle = { onFavouriteToggled(ticker) },
                    onClick = { onSecurityClicked(ticker) },
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))
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
                periodGainPercent = 2.34,
                periodGainAbsolute = 450.20,
                periodDividends = 123.45,
                totalGainPercent = 5.19,
                totalGainAbsolute = 1_200.50,
                lifetimeDividends = 788.40,
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
                periodGainPercent = 2.34,
                periodGainAbsolute = 450.20,
                periodDividends = 123.45,
                totalGainPercent = 5.19,
                totalGainAbsolute = 1_200.50,
                lifetimeDividends = 788.40,
            ),
            onEvent = {},
        )
    }
}
