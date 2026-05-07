package com.akole.dividox.feature.portfolio

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.connectivity.ConnectivityBannerHost
import com.akole.dividox.common.ui.resources.components.connectivity.LocalNetworkConnectivityManager
import com.akole.dividox.common.ui.resources.format.formatPercent
import com.akole.dividox.common.ui.resources.format.formatPrice
import com.akole.dividox.common.ui.resources.format.monthShort
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.common.ui.resources.theme.extendedColors
import com.akole.dividox.common.ui.resources.theme.spacing
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.model.displayName
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.portfolio_edit
import dividox.common.ui_resources.generated.resources.portfolio_per_share
import dividox.common.ui_resources.generated.resources.portfolio_purchase_price
import dividox.common.ui_resources.generated.resources.portfolio_shares_format
import dividox.common.ui_resources.generated.resources.portfolio_empty_state
import dividox.common.ui_resources.generated.resources.portfolio_search_placeholder
import dividox.common.ui_resources.generated.resources.portfolio_sort_date
import dividox.common.ui_resources.generated.resources.portfolio_sort_gain
import dividox.common.ui_resources.generated.resources.portfolio_sort_value
import dividox.common.ui_resources.generated.resources.portfolio_sort_yield
import dividox.common.ui_resources.generated.resources.portfolio_yield_label
import dividox.common.ui_resources.generated.resources.section_portfolio
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun PortfolioScreen(
    state: PortfolioContract.PortfolioViewState,
    onEvent: (PortfolioContract.PortfolioViewEvent) -> Unit,
    sideEffects: Flow<PortfolioContract.PortfolioSideEffect>,
    onNavigation: (PortfolioContract.PortfolioSideEffect.Navigation) -> Unit,
) {
    CollectSideEffect(sideEffects) { effect ->
        when (effect) {
            is PortfolioContract.PortfolioSideEffect.Navigation -> onNavigation(effect)
        }
    }

    PortfolioContent(
        state = state,
        onEvent = onEvent,
    )
}

@Composable
private fun PortfolioContent(
    state: PortfolioContract.PortfolioViewState,
    onEvent: (PortfolioContract.PortfolioViewEvent) -> Unit,
) {
    val connectivityManager = LocalNetworkConnectivityManager.current

    Scaffold(
        topBar = {
            DividoxTopAppBar(
                title = stringResource(Res.string.section_portfolio),
            )
        },
        floatingActionButton = {},
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Banner positioned right below TopAppBar
            ConnectivityBannerHost(connectivityFlow = connectivityManager.observeConnectivity())

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.spacing.medium)
                            .weight(1f),
                    ) {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                        SearchBar(
                            query = state.searchQuery,
                            onQueryChanged = { query ->
                                onEvent(PortfolioContract.PortfolioViewEvent.SearchQueryChanged(query))
                            },
                        )

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                        SortChips(
                            selectedOrder = state.sortOrder,
                            onOrderChanged = { order ->
                                onEvent(PortfolioContract.PortfolioViewEvent.SortOrderChanged(order))
                            },
                        )

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                        if (state.holdings.isEmpty()) {
                            EmptyState()
                        } else {
                            HoldingsList(
                                holdings = state.holdings,
                                currency = state.currency,
                                convertedPrices = state.convertedPrices,
                                onSecurityClicked = { ticker ->
                                    onEvent(PortfolioContract.PortfolioViewEvent.SecurityClicked(ticker))
                                },
                                onEditClicked = { holdingId ->
                                    onEvent(PortfolioContract.PortfolioViewEvent.EditHoldingClicked(holdingId))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Local state so TextField updates instantly without waiting for the ViewModel round-trip.
    // LaunchedEffect syncs back if the query is cleared externally (e.g. on screen reset).
    var localQuery by remember { mutableStateOf(query) }
    LaunchedEffect(query) {
        if (localQuery != query) localQuery = query
    }
    TextField(
        value = localQuery,
        onValueChange = {
            localQuery = it
            onQueryChanged(it)
        },
        modifier = modifier
            .fillMaxWidth(),
        placeholder = { Text(stringResource(Res.string.portfolio_search_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun SortChips(
    selectedOrder: SortOrder,
    onOrderChanged: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SortField.entries.forEach { field ->
            val isSelected = field == selectedOrder.field
            val arrow = if (isSelected) {
                if (selectedOrder.ascending) " ↑" else " ↓"
            } else {
                ""
            }
            SortChip(
                label = getSortFieldLabel(field) + arrow,
                isSelected = isSelected,
                onClick = { onOrderChanged(selectedOrder.toggle(field)) },
            )
        }
    }
}

@Composable
private fun getSortFieldLabel(field: SortField): String {
    return stringResource(
        when (field) {
            SortField.GAIN -> Res.string.portfolio_sort_gain
            SortField.VALUE -> Res.string.portfolio_sort_value
            SortField.DIVIDEND -> Res.string.portfolio_sort_yield
            SortField.DATE -> Res.string.portfolio_sort_date
        },
    )
}

@Composable
private fun SortChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        modifier = modifier.clickable { onClick() },
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
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.small,
                vertical = MaterialTheme.spacing.xSmall,
            ),
        )
    }
}

@Composable
private fun HoldingsList(
    holdings: List<SecurityHolding>,
    currency: Currency,
    convertedPrices: Map<String, Double>,
    onSecurityClicked: (String) -> Unit,
    onEditClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(holdings) { holding ->
            HoldingCard(
                holding = holding,
                currency = currency,
                displayPrice = convertedPrices[holding.holding.tickerId] ?: holding.quote.price,
                onSecurityClicked = onSecurityClicked,
                onEditClicked = onEditClicked,
            )
        }
    }
}

@Composable
private fun HoldingCard(
    holding: SecurityHolding,
    currency: Currency,
    displayPrice: Double,
    onSecurityClicked: (String) -> Unit,
    onEditClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ticker = holding.holding.tickerId
    val shares = holding.holding.shares
    val gain = holding.totalGainPercent
    val yield = holding.dividendInfo?.yield ?: 0.0
    val purchaseDate = Instant.fromEpochMilliseconds(holding.holding.purchaseDate)
        .toLocalDateTime(TimeZone.UTC).date
    val purchaseDateLabel = "${purchaseDate.day} ${purchaseDate.monthShort()} ${purchaseDate.year}"
    val gainColor = when {
        gain > 0.0 -> MaterialTheme.extendedColors.profit
        gain < 0.0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSecurityClicked(ticker) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
        ) {
            // Header row: name+ticker (left) | value+per-share+gain (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = holding.quote.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = ticker,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Text(
                        text = stringResource(Res.string.portfolio_shares_format, shares.formatShares()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = (shares * displayPrice).formatPrice(currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Text(
                        text = displayPrice.formatPrice(currency) + stringResource(Res.string.portfolio_per_share),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))
                    Text(
                        text = gain.formatPercent(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = gainColor,
                        maxLines = 1,
                        modifier = Modifier
                            .background(
                                color = gainColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            // Row 3: Purchase label + price · date
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.portfolio_purchase_price),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Text(
                    text = holding.holding.purchasePrice.formatPrice(holding.holding.purchaseCurrency) + stringResource(Res.string.portfolio_per_share) + " · $purchaseDateLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))

            // Row 4: yield | edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                Text(
                    text = stringResource(Res.string.portfolio_yield_label, yield.formatPercent()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(Res.string.portfolio_edit),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onEditClicked(holding.holding.id.value) },
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.portfolio_empty_state),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun Double.formatShares(): String {
    val long = toLong()
    return if (this == long.toDouble()) long.toString() else toString()
}

// region Previews

private val previewHoldings = listOf(
    SecurityHolding(
        holding = Holding(
            id = HoldingId("h1"),
            tickerId = "AAPL",
            shares = 10.0,
            purchasePrice = 150.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 1_700_000_000_000L,
        ),
        quote = StockQuote(
            ticker = "AAPL",
            price = 189.50,
            change = 2.35,
            changePercent = 1.25,
            currency = "USD",
            lastUpdated = Instant.fromEpochMilliseconds(1_714_650_000_000L),
        ),
        dividendInfo = DividendInfo(
            ticker = "AAPL",
            yield = 0.52,
            annualPayout = 0.96,
            payoutRatio = 15.0,
            fiveYearGrowth = 5.8,
            exDividendDate = LocalDate(2026, 5, 9),
        ),
        totalGainPercent = 26.33,
    ),
    SecurityHolding(
        holding = Holding(
            id = HoldingId("h2"),
            tickerId = "MSFT",
            shares = 5.0,
            purchasePrice = 320.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 1_690_000_000_000L,
        ),
        quote = StockQuote(
            ticker = "MSFT",
            price = 415.80,
            change = -1.20,
            changePercent = -0.29,
            currency = "USD",
            lastUpdated = Instant.fromEpochMilliseconds(1_714_650_000_000L),
        ),
        dividendInfo = DividendInfo(
            ticker = "MSFT",
            yield = 0.72,
            annualPayout = 3.00,
            payoutRatio = 25.0,
            fiveYearGrowth = 10.2,
            exDividendDate = LocalDate(2026, 5, 15),
        ),
        totalGainPercent = 29.94,
    ),
    SecurityHolding(
        holding = Holding(
            id = HoldingId("h3"),
            tickerId = "O",
            shares = 50.0,
            purchasePrice = 62.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 1_680_000_000_000L,
        ),
        quote = StockQuote(
            ticker = "O",
            price = 57.30,
            change = 0.15,
            changePercent = 0.26,
            currency = "USD",
            lastUpdated = Instant.fromEpochMilliseconds(1_714_650_000_000L),
        ),
        dividendInfo = DividendInfo(
            ticker = "O",
            yield = 5.45,
            annualPayout = 3.07,
            payoutRatio = 75.0,
            fiveYearGrowth = 3.1,
            exDividendDate = LocalDate(2026, 4, 30),
        ),
        totalGainPercent = -7.58,
    ),
)

@Preview
@Composable
private fun PortfolioScreenPreview() {
    DividoxTheme {
        PortfolioScreen(
            state = PortfolioContract.PortfolioViewState(
                isLoading = false,
                holdings = previewHoldings,
            ),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}

@Preview
@Composable
private fun PortfolioScreenEmptyPreview() {
    DividoxTheme {
        PortfolioScreen(
            state = PortfolioContract.PortfolioViewState(
                isLoading = false,
                holdings = emptyList(),
            ),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}

@Preview
@Composable
private fun PortfolioScreenLoadingPreview() {
    DividoxTheme {
        PortfolioScreen(
            state = PortfolioContract.PortfolioViewState(
                isLoading = true,
            ),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}

@Preview
@Composable
private fun PortfolioScreenDarkPreview() {
    DividoxTheme(darkTheme = true) {
        PortfolioScreen(
            state = PortfolioContract.PortfolioViewState(
                isLoading = false,
                holdings = previewHoldings,
            ),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}

// endregion
