package com.akole.dividox.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.ui.resources.components.DisclaimerBanner
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.MarketFilterRow
import com.akole.dividox.common.ui.resources.components.SearchBar
import com.akole.dividox.common.ui.resources.components.SecurityCard
import com.akole.dividox.common.ui.resources.components.connectivity.ConnectivityBannerHost
import com.akole.dividox.common.ui.resources.components.connectivity.LocalNetworkConnectivityManager
import com.akole.dividox.common.ui.resources.theme.spacing
import com.akole.dividox.component.market.domain.model.SecurityType
import com.akole.dividox.feature.search.SearchContract.SearchSideEffect
import com.akole.dividox.feature.search.SearchContract.SearchSideEffect.Navigation
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.BackClicked
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.FavouriteToggled
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.MarketFilterChanged
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.QueryChanged
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.SecurityClicked
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.TypeFilterChanged
import com.akole.dividox.feature.search.SearchContract.SearchViewState
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.search_no_results
import dividox.common.ui_resources.generated.resources.search_placeholder_hint
import dividox.common.ui_resources.generated.resources.search_title
import dividox.common.ui_resources.generated.resources.security_type_all
import dividox.common.ui_resources.generated.resources.security_type_equity
import dividox.common.ui_resources.generated.resources.security_type_etf
import dividox.common.ui_resources.generated.resources.security_type_fund
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchScreen(
    state: SearchViewState,
    sideEffect: Flow<SearchSideEffect> = emptyFlow(),
    onEvent: (SearchViewEvent) -> Unit = {},
    onNavigation: (Navigation) -> Unit = {},
) {
    CollectSideEffect(sideEffect) { effect ->
        if (effect is Navigation) onNavigation(effect)
    }

    val connectivityManager = LocalNetworkConnectivityManager.current

    Scaffold(
        topBar = {
            DividoxTopAppBar(
                title = stringResource(Res.string.search_title),
                onBack = { onEvent(BackClicked) },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = MaterialTheme.spacing.medium),
        ) {
            item {
                ConnectivityBannerHost(connectivityFlow = connectivityManager.observeConnectivity())
            }

            item {
                SearchBar(
                    query = state.query,
                    onQueryChange = { onEvent(QueryChanged(it)) },
                    placeholder = stringResource(Res.string.search_placeholder_hint),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.small,
                        ),
                    autoFocus = true,
                )
            }

            item {
                MarketFilterRow(
                    selectedMarket = state.selectedMarket,
                    onMarketSelected = { onEvent(MarketFilterChanged(it)) },
                )
            }

            item {
                SecurityTypeFilterRow(
                    selectedType = state.selectedType,
                    onTypeSelected = { onEvent(TypeFilterChanged(it)) },
                )
            }

            when {
                state.isLoading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = MaterialTheme.spacing.xLarge),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.query.isNotBlank() && state.results.isEmpty() -> item {
                    Text(
                        text = stringResource(Res.string.search_no_results, state.query),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.large),
                    )
                }

                else -> items(state.results, key = { it.ticker }) { quote ->
                    SecurityCard(
                        ticker = quote.ticker,
                        companyName = quote.name,
                        price = null,
                        changePercent = null,
                        currency = Currency.USD,
                        isFavorite = quote.ticker in state.watchlistedTickers,
                        isInPortfolio = false,
                        onFavoriteToggle = { onEvent(FavouriteToggled(quote.ticker)) },
                        onClick = { onEvent(SecurityClicked(quote.ticker)) },
                        securityType = quote.type.label(),
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.xSmall,
                        ),
                    )
                }
            }

            item {
                DisclaimerBanner(
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.small),
                )
            }
        }
    }
}

@Composable
private fun SecurityTypeFilterRow(
    selectedType: SecurityType?,
    onTypeSelected: (SecurityType?) -> Unit,
) {
    val options: List<SecurityType?> = listOf(null) + SecurityType.entries
    LazyRow(
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        items(options, key = { it?.name ?: "ALL" }) { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        text = type.label() ?: stringResource(Res.string.security_type_all),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
            )
        }
    }
}

@Composable
private fun SecurityType?.label(): String? = when (this) {
    SecurityType.EQUITY -> stringResource(Res.string.security_type_equity)
    SecurityType.ETF -> stringResource(Res.string.security_type_etf)
    SecurityType.MUTUAL_FUND -> stringResource(Res.string.security_type_fund)
    null -> null
}
