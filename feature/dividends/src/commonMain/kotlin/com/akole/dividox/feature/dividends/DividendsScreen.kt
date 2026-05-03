package com.akole.dividox.feature.dividends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.ui.resources.charts.BarChart
import com.akole.dividox.common.ui.resources.charts.BarChartEntry
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.connectivity.ConnectivityBannerHost
import com.akole.dividox.common.ui.resources.components.connectivity.LocalNetworkConnectivityManager
import com.akole.dividox.common.ui.resources.format.formatPercent
import com.akole.dividox.common.ui.resources.format.formatPercentSigned
import com.akole.dividox.common.ui.resources.format.formatTwoDecimals
import com.akole.dividox.common.ui.resources.theme.extendedColors
import com.akole.dividox.common.ui.resources.theme.spacing
import com.akole.dividox.feature.dividends.DividendsContract.DividendsSideEffect
import com.akole.dividox.feature.dividends.DividendsContract.DividendsViewEvent
import com.akole.dividox.feature.dividends.DividendsContract.DividendsViewState
import com.akole.dividox.integration.dividend.domain.model.DividendActivitySummary
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import com.akole.dividox.integration.dividend.domain.model.MonthBar
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

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
            DividoxTopAppBar(title = "Dividend Activity")
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            ConnectivityBannerHost(connectivityFlow = connectivityManager.observeConnectivity())

            when {
                state.isLoading -> LoadingContent(modifier = Modifier.weight(1f))
                state.error != null -> ErrorContent(
                    message = state.error,
                    onRetry = { onEvent(DividendsViewEvent.Refresh) },
                    modifier = Modifier.weight(1f),
                )
                else -> DividendsContent(
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier.weight(1f),
                )
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
                text = "Tap to retry",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .padding(top = MaterialTheme.spacing.medium),
                )
            }
        }

        // Section 2 — Projection Chart
        if (state.projectionBars.isNotEmpty()) {
            item {
                ProjectionChartSection(
                    bars = state.projectionBars,
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
                    title = "Upcoming Payments",
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                )
            }
            items(state.upcomingPayments) { enrichedPayment ->
                UpcomingPaymentRow(
                    enrichedPayment = enrichedPayment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onEvent(DividendsViewEvent.PaymentClicked(enrichedPayment.payment.tickerId))
                        }
                        .padding(horizontal = MaterialTheme.spacing.medium),
                )
            }
        }

        // Section 4 — Past Activity
        if (state.historyByMonth.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Past Activity",
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                )
            }
            state.historyByMonth.forEach { (yearMonth, payments) ->
                val isExpanded = yearMonth in state.expandedMonths
                item(key = "month-${yearMonth}") {
                    HistoryMonthGroup(
                        yearMonth = yearMonth,
                        payments = payments,
                        isExpanded = isExpanded,
                        onToggle = { onEvent(DividendsViewEvent.MonthToggled(yearMonth)) },
                        onPaymentClick = { ticker ->
                            onEvent(DividendsViewEvent.HistoryEntryClicked(ticker))
                        },
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                    )
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            // Row 1 — Lifetime | YTD | YoY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MetricItem(
                    label = "Lifetime",
                    value = "$${summary.lifetime.formatTwoDecimals()}",
                    modifier = Modifier.weight(1f),
                )
                VerticalDivider()
                MetricItem(
                    label = "YTD",
                    value = "$${summary.ytd.formatTwoDecimals()}",
                    modifier = Modifier.weight(1f),
                )
                VerticalDivider()
                val yoy = summary.yoyPercent
                val yoyText = yoy?.formatPercentSigned() ?: "—"
                val yoyColor = when {
                    yoy == null -> MaterialTheme.colorScheme.onSurface
                    yoy >= 0 -> MaterialTheme.extendedColors.profit
                    else -> MaterialTheme.colorScheme.error
                }
                MetricItem(
                    label = "YoY",
                    value = yoyText,
                    valueColor = yoyColor,
                    modifier = Modifier.weight(1f),
                )
            }

            HorizontalDivider()

            // Row 2 — Next Payout + YoC
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                val nextPayoutText = summary.nextPayout?.let {
                    val ticker = it.payment.tickerId
                    val date = it.payment.paymentDate.formatShort()
                    "$ticker · $date"
                } ?: "—"
                MetricItem(
                    label = "Next Payout",
                    value = nextPayoutText,
                    modifier = Modifier.weight(1f),
                )
                VerticalDivider()
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "YoC",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = summary.yoc.formatPercent(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    val progress = (summary.yoc / summary.yocTarget).coerceIn(0.0, 1.0).toFloat()
                    val progressColor = if (summary.yoc >= summary.yocTarget) {
                        MaterialTheme.extendedColors.profit
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = progressColor,
                    )
                    Text(
                        text = "Target ${summary.yocTarget.formatPercent()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

// ─── Section 2: Projection Chart ─────────────────────────────────────────────

@Composable
private fun ProjectionChartSection(
    bars: List<MonthBar>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Dividend Projection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = "Last 12 months",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            Spacer(Modifier.height(MaterialTheme.spacing.small))

            val entries = bars.map { bar ->
                BarChartEntry(
                    label = bar.yearMonth.monthShort(),
                    value = bar.amount.toFloat(),
                )
            }

            BarChart(
                entries = entries,
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                barColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// ─── Section 3: Upcoming Payments ────────────────────────────────────────────

@Composable
private fun UpcomingPaymentRow(
    enrichedPayment: EnrichedPayment,
    modifier: Modifier = Modifier,
) {
    val payment = enrichedPayment.payment
    Row(
        modifier = modifier.padding(vertical = MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        CompanyLogo(ticker = payment.tickerId)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = payment.tickerId,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            val companyName = enrichedPayment.companyInfo?.name
            if (companyName != null) {
                Text(
                    text = companyName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${payment.currency} ${payment.amount.formatTwoDecimals()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = payment.paymentDate.formatShort(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

// ─── Section 4: Past Activity ─────────────────────────────────────────────────

@Composable
private fun HistoryMonthGroup(
    yearMonth: LocalDate,
    payments: List<EnrichedPayment>,
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
                text = "${yearMonth.month.displayName()} ${yearMonth.year}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val monthTotal: Double = payments.sumOf { it.payment.amount }
                Text(
                    text = "$${monthTotal.formatTwoDecimals()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                )
            }
        }

        if (isExpanded) {
            payments.forEach { enrichedPayment ->
                DividendHistoryRow(
                    enrichedPayment = enrichedPayment,
                    onClick = { onPaymentClick(enrichedPayment.payment.tickerId) },
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun DividendHistoryRow(
    enrichedPayment: EnrichedPayment,
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
                text = "${payment.currency} ${payment.amount.formatTwoDecimals()}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ─── Shared / Utility ─────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
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

// ─── Date formatting extensions ───────────────────────────────────────────────

private fun LocalDate.formatShort(): String =
    "${dayOfMonth.toString().padStart(2, '0')}/${monthNumber.toString().padStart(2, '0')}/$year"

private fun LocalDate.monthShort(): String = month.displayName().take(3)

private fun Month.displayName(): String = when (this) {
    Month.JANUARY -> "January"
    Month.FEBRUARY -> "February"
    Month.MARCH -> "March"
    Month.APRIL -> "April"
    Month.MAY -> "May"
    Month.JUNE -> "June"
    Month.JULY -> "July"
    Month.AUGUST -> "August"
    Month.SEPTEMBER -> "September"
    Month.OCTOBER -> "October"
    Month.NOVEMBER -> "November"
    Month.DECEMBER -> "December"
    else -> "Unknown"
}

