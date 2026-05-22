package com.akole.dividox.feature.analysis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.ui.resources.charts.BarChart
import com.akole.dividox.common.ui.resources.charts.BarChartEntry
import com.akole.dividox.common.ui.resources.charts.LineChart
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.NewsSection
import com.akole.dividox.common.ui.resources.format.formatBarChartPopupLabel
import com.akole.dividox.common.ui.resources.format.formatLargeNumber
import com.akole.dividox.common.ui.resources.format.formatPercentSigned
import com.akole.dividox.common.ui.resources.format.formatPrice
import com.akole.dividox.common.ui.resources.format.formatTwoDecimals
import com.akole.dividox.common.ui.resources.theme.extendedColors
import com.akole.dividox.common.ui.resources.theme.spacing
import com.akole.dividox.feature.analysis.SecurityDetailContract.SecurityDetailSideEffect
import com.akole.dividox.feature.analysis.SecurityDetailContract.SecurityDetailViewEvent
import com.akole.dividox.feature.analysis.SecurityDetailContract.SecurityDetailViewState
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.analysis_add_security
import dividox.common.ui_resources.generated.resources.analysis_dividend_growth
import dividox.common.ui_resources.generated.resources.analysis_dividend_metrics
import dividox.common.ui_resources.generated.resources.analysis_edit_holding
import dividox.common.ui_resources.generated.resources.analysis_error_loading
import dividox.common.ui_resources.generated.resources.analysis_fundamentals
import dividox.common.ui_resources.generated.resources.analysis_price_chart
import dividox.common.ui_resources.generated.resources.analysis_retry
import dividox.common.ui_resources.generated.resources.analysis_metric_5y_growth
import dividox.common.ui_resources.generated.resources.analysis_metric_annual_payout
import dividox.common.ui_resources.generated.resources.analysis_metric_payout_ratio
import dividox.common.ui_resources.generated.resources.analysis_metric_yield
import dividox.common.ui_resources.generated.resources.analysis_view_as_percent
import dividox.common.ui_resources.generated.resources.analysis_view_as_value
import dividox.common.ui_resources.generated.resources.cd_remove_from_favourites
import dividox.common.ui_resources.generated.resources.disclaimer_prices_delayed
import dividox.common.ui_resources.generated.resources.metric_52w_high
import dividox.common.ui_resources.generated.resources.metric_52w_low
import dividox.common.ui_resources.generated.resources.metric_day_range
import dividox.common.ui_resources.generated.resources.metric_ex_dividend_date
import dividox.common.ui_resources.generated.resources.metric_next_dividend_date
import dividox.common.ui_resources.generated.resources.metric_volume
import dividox.common.ui_resources.generated.resources.period_1d
import dividox.common.ui_resources.generated.resources.period_1m
import dividox.common.ui_resources.generated.resources.period_1w
import dividox.common.ui_resources.generated.resources.period_1y
import dividox.common.ui_resources.generated.resources.period_5y
import dividox.common.ui_resources.generated.resources.period_all
import dividox.common.ui_resources.generated.resources.period_ytd
import com.akole.dividox.component.market.domain.model.ChartPeriod
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource

private val PriceChartHeight = 200.dp
private val DividendChartHeight = 250.dp

@Suppress("LongMethod")
@Composable
fun SecurityDetailScreen(
    state: SecurityDetailViewState,
    onEvent: (SecurityDetailViewEvent) -> Unit,
    sideEffects: Flow<SecurityDetailSideEffect>,
    onNavigation: (SecurityDetailSideEffect.Navigation) -> Unit,
) {
    CollectSideEffect(sideEffects) { effect ->
        when (effect) {
            is SecurityDetailSideEffect.Navigation -> onNavigation(effect)
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        onEvent(SecurityDetailViewEvent.OnLoad)
    }

    Scaffold(
        topBar = {
            DividoxTopAppBar(
                title = state.companyInfo?.name ?: state.ticker,
                onBack = { onEvent(SecurityDetailViewEvent.OnBackClicked) },
                actions = {
                    IconButton(onClick = { onEvent(SecurityDetailViewEvent.OnFavoriteToggled) }) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(Res.string.cd_remove_from_favourites),
                            tint = if (state.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.analysis_error_loading),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    Button(onClick = { onEvent(SecurityDetailViewEvent.OnLoad) }) {
                        Text(stringResource(Res.string.analysis_retry))
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    if (state.quote != null) {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                        PriceCard(state = state)
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                        ChartSection(state = state, onEvent = onEvent)
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                        if (state.dividendInfo != null) {
                            DividendMetricsSection(state = state)
                            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                            DividendGrowthSection(state = state, onEvent = onEvent)
                            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                        }

                        FundamentalsSection(state = state)
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                        NewsSection(
                            news = state.news,
                            isLoading = state.newsLoading,
                            compact = true,
                            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                        CtaButton(state = state, onEvent = onEvent)
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                        Text(
                            text = stringResource(Res.string.disclaimer_prices_delayed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = MaterialTheme.spacing.medium,
                                    vertical = MaterialTheme.spacing.small,
                                ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceCard(state: SecurityDetailViewState) {
    val quote = state.quote ?: return
    val isPositive = quote.change >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = quote.price.formatPrice(quote.currency),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (state.companyInfo?.exchange != null) {
                    Text(
                        text = state.companyInfo.exchange,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = MaterialTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${quote.change.formatTwoDecimals()} (${quote.changePercent.formatPercentSigned()})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }
            if (state.lastUpdated != null) {
                Text(
                    text = "Updated: now",
                    modifier = Modifier.padding(top = MaterialTheme.spacing.small),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ChartSection(
    state: SecurityDetailViewState,
    onEvent: (SecurityDetailViewEvent) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Text(
                text = stringResource(Res.string.analysis_price_chart),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            // Period selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
            ) {
                ChartPeriod.entries.forEach { period ->
                    val isSelected = period == state.selectedChartPeriod
                    val label = when (period) {
                        ChartPeriod.ONE_DAY -> stringResource(Res.string.period_1d)
                        ChartPeriod.ONE_WEEK -> stringResource(Res.string.period_1w)
                        ChartPeriod.ONE_MONTH -> stringResource(Res.string.period_1m)
                        ChartPeriod.YTD -> stringResource(Res.string.period_ytd)
                        ChartPeriod.ONE_YEAR -> stringResource(Res.string.period_1y)
                        ChartPeriod.FIVE_YEARS -> stringResource(Res.string.period_5y)
                        ChartPeriod.ALL -> stringResource(Res.string.period_all)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (!isSelected) onEvent(SecurityDetailViewEvent.ChartPeriodSelected(period))
                            },
                    ) {
                        Text(
                            text = label,
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
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            val isChartTransitioning = state.selectedChartPeriod != state.renderedChartPeriod
            val (chartEntries, floorPrice) = state.priceHistory.toPriceChartData(state.renderedChartPeriod)
            Box(modifier = Modifier.fillMaxWidth()) {
                LineChart(
                    entries = chartEntries,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (isChartTransitioning) 0.4f else 1f),
                    height = PriceChartHeight,
                    yAxisValueOffset = floorPrice,
                    popupFormatter = { label, value ->
                        val formattedValue = (value + floorPrice).toDouble().formatPrice(state.quote?.currency ?: "USD")
                        "$formattedValue · $label"
                    },
                )
                if (isChartTransitioning) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}

@Composable
private fun DividendMetricsSection(state: SecurityDetailViewState) {
    val dividendInfo = state.dividendInfo ?: return
    val quoteCurrencyCode = state.quote?.currency ?: "USD"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium),
    ) {
        Text(
            text = stringResource(Res.string.analysis_dividend_metrics),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            MetricCard(
                label = stringResource(Res.string.analysis_metric_yield),
                value = dividendInfo.yield.formatPercentSigned(),
                modifier = Modifier.weight(1f).fillMaxHeight(),
                rawValue = dividendInfo.yield,
            )
            MetricCard(
                label = stringResource(Res.string.analysis_metric_annual_payout),
                value = dividendInfo.annualPayout.formatPrice(quoteCurrencyCode),
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            MetricCard(
                label = stringResource(Res.string.analysis_metric_payout_ratio),
                value = dividendInfo.payoutRatio.formatPercentSigned(),
                modifier = Modifier.weight(1f).fillMaxHeight(),
                rawValue = dividendInfo.payoutRatio,
            )
            MetricCard(
                label = stringResource(Res.string.analysis_metric_5y_growth),
                value = dividendInfo.fiveYearGrowth.formatPercentSigned(),
                modifier = Modifier.weight(1f).fillMaxHeight(),
                rawValue = dividendInfo.fiveYearGrowth,
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier, rawValue: Double? = null) {
    val valueColor = when {
        rawValue == null || rawValue == 0.0 -> MaterialTheme.colorScheme.onSurface
        rawValue > 0 -> MaterialTheme.extendedColors.profit
        else -> MaterialTheme.colorScheme.error
    }
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.small)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
            )
        }
    }
}

@Composable
private fun DividendGrowthSection(
    state: SecurityDetailViewState,
    onEvent: (SecurityDetailViewEvent) -> Unit,
) {
    val quoteCurrency = Currency.entries.firstOrNull { it.code == (state.quote?.currency ?: "USD") } ?: Currency.USD
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.analysis_dividend_growth),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall)) {
                    listOf(
                        false to quoteCurrency.symbol,
                        true to stringResource(Res.string.analysis_view_as_percent)).forEach { (isPercent, label) ->
                        val isSelected = state.isDividendChartPercentage == isPercent
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.clickable {
                                if (!isSelected) onEvent(SecurityDetailViewEvent.ToggleDividendChartMode)
                                                          },
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(
                                    horizontal = MaterialTheme.spacing.small,
                                    vertical = MaterialTheme.spacing.xSmall,
                                ),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            BarChart(
                entries = state.dividendGrowthData.map { bar ->
                    BarChartEntry(
                        label = bar.year.toString(),
                        value = if (state.isDividendChartPercentage) {
                            bar.percentageOfPrice.toFloat()
                        } else {
                            bar.absoluteValue.toFloat()
                        },
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                barHeight = DividendChartHeight,
                barWidth = 16.dp,
                minBarSlotWidth = 28.dp,
                skipAlternateXLabels = true,
                popupLabelFormatter = { entry ->
                    if (state.isDividendChartPercentage) {
                        "${entry.value.toDouble().formatTwoDecimals()}%"
                    } else {
                        formatBarChartPopupLabel(entry.value, quoteCurrency.code, entry.label)
                    }
                },
            )
        }
    }
}

@Composable
private fun FundamentalsSection(state: SecurityDetailViewState) {
    val quote = state.quote ?: return
    val dividendInfo = state.dividendInfo

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium),
    ) {
        Text(
            text = stringResource(Res.string.analysis_fundamentals),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // 52W High | 52W Low
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            MetricCard(
                label = stringResource(Res.string.metric_52w_high),
                value = quote.fiftyTwoWeekHigh?.formatPrice(quote.currency) ?: "N/A",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            MetricCard(
                label = stringResource(Res.string.metric_52w_low),
                value = quote.fiftyTwoWeekLow?.formatPrice(quote.currency) ?: "N/A",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Day Range | Volume
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            val dayRange = if (quote.dayLow != null && quote.dayHigh != null) {
                "${quote.dayLow!!.formatPrice(quote.currency)} – ${quote.dayHigh!!.formatPrice(quote.currency)}"
            } else "N/A"
            MetricCard(
                label = stringResource(Res.string.metric_day_range),
                value = dayRange,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            MetricCard(
                label = stringResource(Res.string.metric_volume),
                value = quote.volume?.toDouble()?.formatLargeNumber() ?: "N/A",
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        // Ex-Div Date | Next Ex-Div (only for dividend-paying stocks)
        if (dividendInfo?.exDividendDate != null || dividendInfo?.nextDividendDate != null) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                MetricCard(
                    label = stringResource(Res.string.metric_ex_dividend_date),
                    value = dividendInfo.exDividendDate?.toString() ?: "N/A",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                MetricCard(
                    label = stringResource(Res.string.metric_next_dividend_date),
                    value = dividendInfo.nextDividendDate?.toString() ?: "N/A",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun CtaButton(
    state: SecurityDetailViewState,
    onEvent: (SecurityDetailViewEvent) -> Unit,
) {
    Button(
        onClick = {
            if (state.isInPortfolio) onEvent(SecurityDetailViewEvent.OnEditHoldingClicked)
            else onEvent(SecurityDetailViewEvent.OnAddSecurityClicked)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .height(MaterialTheme.spacing.buttonMinHeight),
    ) {
        Text(
            text = if (state.isInPortfolio) {
                stringResource(Res.string.analysis_edit_holding)
            } else {
                stringResource(Res.string.analysis_add_security)
            },
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
