package com.akole.dividox.feature.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.ui.resources.components.AnimatedValueText
import com.akole.dividox.common.ui.resources.components.NewsSection
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

/**
 * Very subtle vertical gradient from theme tones: a faint `primaryContainer` tint at the top that
 * fades into the plain (light) `surface` at the bottom. Adapts to light/dark.
 */
@Composable
private fun dashboardBackgroundBrush(): Brush {
    val colors = MaterialTheme.colorScheme
    return remember(colors) {
        Brush.verticalGradient(
            listOf(
                colors.primaryContainer.copy(alpha = GRADIENT_PRIMARY_ALPHA).compositeOver(colors.surface),
                colors.surface,
            ),
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardViewState,
    onEvent: (DashboardViewEvent) -> Unit,
) {
    val connectivityManager = LocalNetworkConnectivityManager.current
    val backgroundBrush = dashboardBackgroundBrush()

    Scaffold(
        modifier = Modifier.background(backgroundBrush),
        containerColor = Color.Transparent,
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
                // Only the top bar inset: the parent scaffold's bottom navigation bar already handles the bottom.
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            ConnectivityBannerHost(connectivityFlow = connectivityManager.observeConnectivity())
            LastUpdatedBar(
                lastUpdated = state.lastUpdated,
                onRefresh = { onEvent(DashboardViewEvent.Refresh) },
            )

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

                    // Each section renders as soon as its slice of state is available.
                    // Loading flags gate individual sections instead of blocking the whole screen.
                    if (state.summaryLoading && state.summary == null) {
                        SectionSkeleton(heightDp = SKELETON_METRICS_HEIGHT)
                    } else {
                        PortfolioOverviewCard(
                            state = state,
                            onPeriodSelected = { onEvent(DashboardViewEvent.PeriodSelected(it)) },
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    if (state.topGainers.isNotEmpty() || state.topLosers.isNotEmpty()) {
                        PortfolioTodaySection(
                            topGainers = state.topGainers,
                            topLosers = state.topLosers,
                            onViewAllClicked = { onEvent(DashboardViewEvent.ViewAllPortfolioClicked) },
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    } else if (state.portfolioTodayLoading) {
                        SectionSkeleton(heightDp = SKELETON_TODAY_HEIGHT)
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    }

                    if (state.watchlistLoading && state.watchlist.isEmpty()) {
                        SectionSkeleton(heightDp = SKELETON_FAVOURITES_HEIGHT)
                    } else {
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
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    MarketIndicesSection(
                        indices = state.marketIndices,
                        isLoading = state.marketIndicesLoading,
                        isError = state.marketIndicesError,
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    NewsSection(
                        news = state.marketNews,
                        isLoading = state.marketNewsLoading,
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                    DisclaimerText()

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
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
    var expanded by retain { mutableStateOf(false) }
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
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = SEGMENT_TRACK_ALPHA))
            .padding(MaterialTheme.spacing.xSmall),
    ) {
        ChartPeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            val background by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            )
            val content by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(period.labelRes()),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = content,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(background)
                    .clickable { onPeriodSelected(period) }
                    .padding(vertical = MaterialTheme.spacing.small),
            )
        }
    }
}

// ─── Portfolio overview ───────────────────────────────────────────────────────

/**
 * Portfolio overview: one hero number (Total value) with the selected-period performance as a
 * coloured pill, a segmented period selector, and a 2×2 grid of labelled all-time stat tiles.
 */
@Composable
private fun PortfolioOverviewCard(
    state: DashboardViewState,
    onPeriodSelected: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currency = state.currency
    // Real values only once the source has emitted; while loading show "—" so empty never looks like zero.
    val summary: PortfolioSummary? = (state.convertedSummary ?: state.summary)?.takeUnless { state.summaryLoading }
    val isEmpty = summary == null || summary.totalValue == 0.0
    val periodReady = summary != null && !state.periodGainLoading

    val cardBrush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = OVERVIEW_GRADIENT_ALPHA),
            MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(OVERVIEW_CORNER_DP.dp),
    ) {
        Column(
            modifier = Modifier
                .background(cardBrush)
                .padding(MaterialTheme.spacing.medium),
        ) {
            Text(
                text = stringResource(Res.string.metric_total_value),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AnimatedValueText(
                value = summary?.totalValue?.formatPrice(currency) ?: PLACEHOLDER_TEXT,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                autoShrink = true,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GainPill(
                    amount = state.periodGainAbsolute,
                    text = if (periodReady) {
                        "${state.periodGainAbsolute.formatPriceSigned(currency)}  ${state.periodGainPercent.formatPercentSigned()}"
                    } else {
                        PLACEHOLDER_TEXT
                    },
                    neutral = isEmpty || !periodReady,
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = stringResource(state.selectedPeriod.labelRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            PeriodSelectorRow(selectedPeriod = state.selectedPeriod, onPeriodSelected = onPeriodSelected)
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.height(IntrinsicSize.Min),
            ) {
                OverviewStatTile(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    label = stringResource(Res.string.metric_invested),
                    value = summary?.let { (it.totalValue - it.totalGain).formatPrice(currency) } ?: PLACEHOLDER_TEXT,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                OverviewStatTile(
                    icon = if (state.totalGainAbsolute < 0) Icons.AutoMirrored.Outlined.TrendingDown else Icons.AutoMirrored.Outlined.TrendingUp,
                    label = stringResource(Res.string.metric_total_return),
                    value = if (summary != null) state.totalGainAbsolute.formatPriceSigned(currency) else PLACEHOLDER_TEXT,
                    caption = if (summary != null) state.totalGainPercent.formatPercentSigned() else null,
                    accent = gainColor(state.totalGainAbsolute, neutral = isEmpty),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.height(IntrinsicSize.Min),
            ) {
                OverviewStatTile(
                    icon = Icons.Outlined.Percent,
                    label = stringResource(Res.string.metric_yield),
                    value = if (state.yieldLoading || summary == null) PLACEHOLDER_PERCENT else summary.totalYield.formatPercent(),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                OverviewStatTile(
                    icon = Icons.Outlined.Payments,
                    label = stringResource(Res.string.metric_dividends_received),
                    value = if (state.lifetimeDividendsLoading) PLACEHOLDER_TEXT else state.lifetimeDividends.formatPrice(currency),
                    caption = if (state.periodDividendsLoading) {
                        null
                    } else {
                        stringResource(
                            Res.string.dashboard_period_dividends,
                            state.periodDividends.formatPrice(currency),
                            stringResource(state.selectedPeriod.labelRes()),
                        )
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun GainPill(amount: Double, text: String, neutral: Boolean) {
    val color = gainColor(amount, neutral)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = PILL_BG_ALPHA))
            .padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.xSmall),
    ) {
        if (!neutral) {
            Icon(
                imageVector = if (amount < 0) Icons.AutoMirrored.Outlined.TrendingDown else Icons.AutoMirrored.Outlined.TrendingUp,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.xSmall))
        }
        AnimatedValueText(
            value = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = color,
            autoShrink = true,
        )
    }
}

@Composable
private fun OverviewStatTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    accent: Color = MaterialTheme.colorScheme.onSurface,
) {
    val iconTint = if (accent == MaterialTheme.colorScheme.onSurface) MaterialTheme.colorScheme.primary else accent
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(TILE_CORNER_DP.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = TILE_BG_ALPHA))
            .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(MaterialTheme.spacing.iconMedium)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = PILL_BG_ALPHA)),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(TILE_ICON_DP.dp),
                )
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))
        AnimatedValueText(
            value = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
            autoShrink = true,
        )
        if (caption != null) {
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = if (accent == MaterialTheme.colorScheme.onSurface) MaterialTheme.colorScheme.onSurfaceVariant else accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun gainColor(amount: Double, neutral: Boolean): Color = when {
    neutral || amount == 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
    amount > 0 -> MaterialTheme.extendedColors.profit
    else -> MaterialTheme.colorScheme.error
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
        DashboardSectionHeader(
            title = stringResource(Res.string.dashboard_portfolio_today),
            onViewAllClicked = onViewAllClicked,
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))

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
        DashboardSectionHeader(
            title = stringResource(Res.string.section_favourites),
            onViewAllClicked = onViewAllClicked.takeIf { watchlist.isNotEmpty() },
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))

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

/**
 * Lightweight per-section loader. Reserves vertical space so the layout doesn't jump
 * when the real data replaces it. Shimmers via onSurface-tinted alpha animation for
 * high contrast on both light and dark themes.
 */
@Composable
private fun SectionSkeleton(heightDp: Int, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "section-shimmer")
    val alpha by transition.animateFloat(
        initialValue = SKELETON_ALPHA_MIN,
        targetValue = SKELETON_ALPHA_MAX,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SKELETON_ANIM_MS),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "section-shimmer-alpha",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha),
                shape = RoundedCornerShape(SKELETON_CORNER_DP.dp),
            ),
    )
}

private const val SKELETON_METRICS_HEIGHT = 290
private const val OVERVIEW_CORNER_DP = 24
private const val TILE_CORNER_DP = 16
private const val TILE_ICON_DP = 14
private const val OVERVIEW_GRADIENT_ALPHA = 0.14f
private const val TILE_BG_ALPHA = 0.7f
private const val PILL_BG_ALPHA = 0.14f
private const val SEGMENT_TRACK_ALPHA = 0.6f
private const val SKELETON_TODAY_HEIGHT = 140
private const val SKELETON_FAVOURITES_HEIGHT = 200

// Match `NewsSection` shimmer: outlineVariant tint, 0.15↔0.4, 800ms.
private const val SKELETON_ALPHA_MIN = 0.15f
private const val SKELETON_ALPHA_MAX = 0.40f
private const val SKELETON_ANIM_MS = 800
private const val SKELETON_CORNER_DP = 16
private const val GRADIENT_PRIMARY_ALPHA = 0.06f

private const val PLACEHOLDER_TEXT = "—"
private const val PLACEHOLDER_PERCENT = "--%"

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
                summaryLoading = false,
                yieldLoading = false,
                periodGainLoading = false,
                portfolioTodayLoading = false,
                watchlistLoading = false,
                lifetimeDividendsLoading = false,
                periodDividendsLoading = false,
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
                summaryLoading = false,
                yieldLoading = false,
                periodGainLoading = false,
                portfolioTodayLoading = false,
                watchlistLoading = false,
                lifetimeDividendsLoading = false,
                periodDividendsLoading = false,
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
                summaryLoading = false,
                yieldLoading = false,
                periodGainLoading = false,
                portfolioTodayLoading = false,
                watchlistLoading = false,
                lifetimeDividendsLoading = false,
                periodDividendsLoading = false,
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
