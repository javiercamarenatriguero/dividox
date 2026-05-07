package com.akole.dividox.feature.dividends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.ui.resources.charts.BarChart
import com.akole.dividox.common.ui.resources.charts.BarChartEntry
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.common.ui.resources.components.DividoxPullToRefreshBox
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.LastUpdatedBar
import com.akole.dividox.common.ui.resources.components.connectivity.ConnectivityBannerHost
import com.akole.dividox.common.ui.resources.components.connectivity.LocalNetworkConnectivityManager
import com.akole.dividox.common.ui.resources.format.formatBarChartPopupLabel
import com.akole.dividox.common.ui.resources.format.formatPercent
import com.akole.dividox.common.ui.resources.format.formatPercentSigned
import com.akole.dividox.common.ui.resources.format.formatPrice
import com.akole.dividox.common.ui.resources.format.formatShort
import com.akole.dividox.common.ui.resources.format.monthFull
import com.akole.dividox.common.ui.resources.theme.extendedColors
import com.akole.dividox.common.ui.resources.theme.spacing
import com.akole.dividox.feature.dividends.DividendsContract.DividendsSideEffect
import com.akole.dividox.feature.dividends.DividendsContract.DividendsViewEvent
import com.akole.dividox.feature.dividends.DividendsContract.DividendsViewState
import com.akole.dividox.integration.dividend.domain.model.DividendActivitySummary
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import com.akole.dividox.integration.dividend.domain.model.MonthBar
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.cd_collapse
import dividox.common.ui_resources.generated.resources.cd_expand
import dividox.common.ui_resources.generated.resources.dividends_empty_state
import dividox.common.ui_resources.generated.resources.dividends_metric_lifetime
import dividox.common.ui_resources.generated.resources.dividends_metric_next_payout
import dividox.common.ui_resources.generated.resources.dividends_metric_ytd
import dividox.common.ui_resources.generated.resources.dividends_metric_yoc
import dividox.common.ui_resources.generated.resources.dividends_metric_yoc_target
import dividox.common.ui_resources.generated.resources.dividends_metric_yoy
import dividox.common.ui_resources.generated.resources.dividends_section_past_activity
import dividox.common.ui_resources.generated.resources.dividends_section_projection
import dividox.common.ui_resources.generated.resources.dividends_ex_date
import dividox.common.ui_resources.generated.resources.dividends_section_upcoming
import dividox.common.ui_resources.generated.resources.dividends_show_less
import dividox.common.ui_resources.generated.resources.dividends_show_more
import dividox.common.ui_resources.generated.resources.dividends_tap_to_retry
import dividox.common.ui_resources.generated.resources.dividends_title
import dividox.common.ui_resources.generated.resources.ui_no_value
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
fun DividendsScreen(
    state: DividendsViewState,
    onEvent: (DividendsViewEvent) -> Unit,
    sideEffects: Flow<DividendsSideEffect>,
    onNavigation: (DividendsSideEffect.Navigation) -> Unit,
) {
    CollectSideEffect(sideEffects) { effect ->
        when (effect) {
            is DividendsSideEffect.Navigation -> onNavigation(effect)
        }
    }

    val connectivityManager = LocalNetworkConnectivityManager.current

    Scaffold(
        topBar = {
            DividoxTopAppBar(title = stringResource(Res.string.dividends_title))
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            ConnectivityBannerHost(connectivityFlow = connectivityManager.observeConnectivity())
            LastUpdatedBar(
                lastUpdated = state.lastUpdated,
                onRefresh = { onEvent(DividendsViewEvent.Refresh) },
            )

            when {
                state.isLoading -> LoadingContent(modifier = Modifier.weight(1f))
                state.error != null -> ErrorContent(
                    message = state.error,
                    onRetry = { onEvent(DividendsViewEvent.Refresh) },
                    modifier = Modifier.weight(1f),
                )
                else -> DividoxPullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onEvent(DividendsViewEvent.Refresh) },
                    modifier = Modifier.weight(1f),
                ) {
                    DividendsContent(
                        state = state,
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.dividends_tap_to_retry),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onRetry),
            )
        }
    }
}

@Composable
private fun DividendsContent(
    state: DividendsViewState,
    onEvent: (DividendsViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        // Section 1 — Critical Metrics
        item {
            state.summary?.let { summary ->
                DividendMetricsBlock(
                    summary = summary,
                    currency = state.currency,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .padding(top = MaterialTheme.spacing.medium),
                )
            }
        }

        // Section 2 — Projection Chart
        if (state.projectionBars.isNotEmpty() || state.selectedRange != DividendHistoryRange.MAX) {
            item {
                ProjectionChartSection(
                    bars = state.projectionBars,
                    selectedRange = state.selectedRange,
                    currency = state.currency,
                    onRangeSelected = { onEvent(DividendsViewEvent.RangeSelected(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                )
            }
        }

        // Section 3 — Upcoming Payments
        if (state.upcomingPayments.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(Res.string.dividends_section_upcoming),
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                )
            }
            val visibleUpcoming = if (state.upcomingExpanded) state.upcomingPayments else state.upcomingPayments.take(3)
            val upcomingByMonth = visibleUpcoming.groupBy {
                LocalDate(it.payment.paymentDate.year, it.payment.paymentDate.month, 1)
            }
            upcomingByMonth.forEach { (yearMonth, payments) ->
                item(key = "upcoming-month-${yearMonth}") {
                    UpcomingMonthHeader(yearMonth = yearMonth)
                }
                items(payments, key = { it.payment.id.value }) { enrichedPayment ->
                    UpcomingPaymentRow(
                        enrichedPayment = enrichedPayment,
                        currency = state.currency,
                        onClick = { onEvent(DividendsViewEvent.PaymentClicked(enrichedPayment.payment.tickerId)) },
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                    )
                }
            }
            if (state.upcomingPayments.size > 3) {
                item {
                    TextButton(
                        onClick = { onEvent(DividendsViewEvent.ToggleUpcomingExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.spacing.medium),
                    ) {
                        val remaining = state.upcomingPayments.size - 3
                        Text(
                            text = if (state.upcomingExpanded) {
                                stringResource(Res.string.dividends_show_less)
                            } else {
                                stringResource(Res.string.dividends_show_more, remaining)
                            },
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }

        // Section 4 — Past Activity
        if (state.historyByMonth.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(Res.string.dividends_section_past_activity),
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                )
            }
            val monthKeys = state.historyByMonth.keys.toList()
            val visibleMonths = if (state.pastActivityExpanded) monthKeys else monthKeys.take(3)
            var lastSeenYear = -1
            visibleMonths.forEach { yearMonth ->
                val payments = state.historyByMonth[yearMonth] ?: return@forEach
                if (yearMonth.year != lastSeenYear) {
                    lastSeenYear = yearMonth.year
                    item(key = "year-${yearMonth.year}") {
                        YearHeader(year = yearMonth.year)
                    }
                }
                val isExpanded = yearMonth in state.expandedMonths
                item(key = "month-${yearMonth}") {
                    HistoryMonthGroup(
                        yearMonth = yearMonth,
                        payments = payments,
                        currency = state.currency,
                        isExpanded = isExpanded,
                        onToggle = { onEvent(DividendsViewEvent.MonthToggled(yearMonth)) },
                        onPaymentClick = { ticker ->
                            onEvent(DividendsViewEvent.HistoryEntryClicked(ticker))
                        },
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                    )
                }
            }
            if (monthKeys.size > 3) {
                item {
                    TextButton(
                        onClick = { onEvent(DividendsViewEvent.TogglePastActivityExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.spacing.medium),
                    ) {
                        val remaining = monthKeys.size - 3
                        Text(
                            text = if (state.pastActivityExpanded) {
                                stringResource(Res.string.dividends_show_less)
                            } else {
                                stringResource(Res.string.dividends_show_more, remaining)
                            },
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(MaterialTheme.spacing.medium)) }
    }
}

// ─── Section 1: Metrics ──────────────────────────────────────────────────────

@Composable
private fun DividendMetricsBlock(
    summary: DividendActivitySummary,
    currency: Currency,
    modifier: Modifier = Modifier,
) {
    val yoy = summary.yoyPercent
    val yoyColor = when {
        yoy == null -> MaterialTheme.colorScheme.onSurfaceVariant
        yoy >= 0 -> MaterialTheme.extendedColors.profit
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column {
            // ── Hero: Lifetime ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .padding(top = MaterialTheme.spacing.large, bottom = MaterialTheme.spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.dividends_metric_lifetime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.xSmall))
                Text(
                    text = summary.lifetime.formatPrice(currency),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            // ── YoY accent strip ────────────────────────────────────────────
            if (yoy != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(yoyColor.copy(alpha = 0.10f))
                        .padding(horizontal = MaterialTheme.spacing.medium, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.dividends_metric_yoy),
                        style = MaterialTheme.typography.labelSmall,
                        color = yoyColor,
                    )
                    Spacer(Modifier.width(MaterialTheme.spacing.xSmall))
                    Text(
                        text = yoy.formatPercentSigned(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = yoyColor,
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            // ── Row: YTD | Next Payout ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(MaterialTheme.spacing.medium),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MetricItem(
                    label = stringResource(Res.string.dividends_metric_ytd),
                    value = summary.ytd.formatPrice(currency),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
                val nextPayout = summary.nextPayout
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = MaterialTheme.spacing.small),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.dividends_metric_next_payout),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(2.dp))
                    if (nextPayout != null) {
                        Text(
                            text = nextPayout.payment.tickerId,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = nextPayout.payment.paymentDate.formatShort(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.ui_no_value),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            // ── YoC progress ────────────────────────────────────────────────
            val progress = (summary.yoc / summary.yocTarget).coerceIn(0.0, 1.0).toFloat()
            val progressColor = if (summary.yoc >= summary.yocTarget) {
                MaterialTheme.extendedColors.profit
            } else {
                MaterialTheme.colorScheme.primary
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.dividends_metric_yoc),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            Res.string.dividends_metric_yoc_target,
                            summary.yocTarget.formatPercent(),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(MaterialTheme.spacing.xSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                    Text(
                        text = summary.yoc.formatPercent(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = progressColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ─── Section 2: Projection Chart ─────────────────────────────────────────────

@Composable
private fun ProjectionChartSection(
    bars: List<MonthBar>,
    selectedRange: DividendHistoryRange,
    currency: Currency,
    onRangeSelected: (DividendHistoryRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Text(
                text = stringResource(Res.string.dividends_section_projection),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.small))
            DividendRangeSelectorRow(
                selectedRange = selectedRange,
                onRangeSelected = onRangeSelected,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(MaterialTheme.spacing.small))

            val entries: List<BarChartEntry> = bars.map { bar ->
                BarChartEntry(
                    label = bar.yearMonth.toBarLabel(selectedRange),
                    value = bar.amount.toFloat(),
                )
            }

            val hasData = bars.any { it.amount > 0.0 }
            if (!hasData) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.dividends_empty_state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val barWidth: Dp = when (selectedRange) {
                    DividendHistoryRange.YTD,
                    DividendHistoryRange.ONE_YEAR -> 24.dp
                    DividendHistoryRange.FIVE_YEARS,
                    DividendHistoryRange.MAX -> 28.dp
                }
                BarChart(
                    entries = entries,
                    modifier = Modifier.fillMaxWidth(),
                    barColor = MaterialTheme.colorScheme.primary,
                    barWidth = barWidth,
                    minBarSlotWidth = barWidth + 20.dp,
                    skipAlternateXLabels = entries.size > 6,
                    popupLabelFormatter = { entry ->
                        formatBarChartPopupLabel(entry.value, currency.code, entry.label)
                    },
                )
            }
        }
    }
}

@Composable
private fun DividendRangeSelectorRow(
    selectedRange: DividendHistoryRange,
    onRangeSelected: (DividendHistoryRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DividendHistoryRange.entries.forEach { range ->
            val isSelected = range == selectedRange
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier
                    .weight(1f)
                    .clickable { onRangeSelected(range) },
            ) {
                Text(
                    text = stringResource(range.labelRes()),
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

private fun LocalDate.monthYearLabel(): String = "${monthFull()} $year"

// ─── Section 4: Past Activity ─────────────────────────────────────────────────

@Composable
private fun UpcomingMonthHeader(yearMonth: LocalDate, modifier: Modifier = Modifier) {
    Text(
        text = yearMonth.monthYearLabel(),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(
            horizontal = MaterialTheme.spacing.medium,
            vertical = MaterialTheme.spacing.xSmall,
        ),
    )
}

@Composable
private fun YearHeader(year: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .padding(top = MaterialTheme.spacing.medium, bottom = MaterialTheme.spacing.xSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun HistoryMonthGroup(
    yearMonth: LocalDate,
    payments: List<EnrichedPayment>,
    currency: Currency,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onPaymentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = MaterialTheme.spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = yearMonth.monthYearLabel(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val monthTotal: Double = payments.sumOf { it.payment.amount }
                Text(
                    text = monthTotal.formatPrice(currency),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) stringResource(Res.string.cd_collapse) else stringResource(Res.string.cd_expand),
                )
            }
        }

        if (isExpanded) {
            payments.forEach { enrichedPayment ->
                DividendHistoryRow(
                    enrichedPayment = enrichedPayment,
                    currency = currency,
                    onClick = { onPaymentClick(enrichedPayment.payment.tickerId) },
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun UpcomingPaymentRow(
    enrichedPayment: EnrichedPayment,
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val payment = enrichedPayment.payment
    val info = enrichedPayment.dividendInfo

    // Upcoming ex-date: prefer the projected future ex-date from DividendInfo, fall back to the
    // paymentDate already projected by DividendProjectionCalculator (both are the next ex-date)
    val exDate = info?.nextDividendDate ?: payment.paymentDate

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        CompanyLogo(ticker = payment.tickerId, size = 32)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = payment.tickerId,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.dividends_ex_date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = exDate.formatShort(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = payment.amount.formatPrice(currency),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (payment.amountPerShare > 0.0) {
                Text(
                    text = "${payment.amountPerShare.formatPrice(currency)} × ${payment.shares.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DividendHistoryRow(
    enrichedPayment: EnrichedPayment,
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val payment = enrichedPayment.payment
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        CompanyLogo(ticker = payment.tickerId, size = 32)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = payment.tickerId,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = payment.paymentDate.formatShort(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = payment.amount.formatPrice(currency),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (payment.amountPerShare > 0.0) {
                Text(
                    text = "${payment.amountPerShare.formatPrice(currency)} × ${payment.shares.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─── Shared / Utility ─────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
}

@Composable
private fun CompanyLogo(ticker: String, size: Int = 40) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = ticker.take(2).uppercase(),
            style = if (size >= 40) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
