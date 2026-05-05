package com.akole.dividox.feature.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.ui.resources.components.DisclaimerBanner
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.SearchBar
import com.akole.dividox.common.ui.resources.components.SecurityCard
import com.akole.dividox.common.ui.resources.components.connectivity.ConnectivityBannerHost
import com.akole.dividox.common.ui.resources.components.connectivity.LocalNetworkConnectivityManager
import com.akole.dividox.common.ui.resources.theme.spacing
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesSideEffect
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesSideEffect.Navigation
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewEvent
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewEvent.BackClicked
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewEvent.FavoriteToggled
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewEvent.SearchQueryChanged
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewEvent.SecurityClicked
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewState
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.favorites_title
import dividox.common.ui_resources.generated.resources.favourites_empty_hint
import dividox.common.ui_resources.generated.resources.search_security_hint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource

@Composable
fun FavoritesScreen(
    state: FavoritesViewState,
    sideEffect: Flow<FavoritesSideEffect> = emptyFlow(),
    onEvent: (FavoritesViewEvent) -> Unit = {},
    onNavigation: (Navigation) -> Unit = {},
) {
    CollectSideEffect(sideEffect) { effect ->
        if (effect is Navigation) onNavigation(effect)
    }

    val connectivityManager = LocalNetworkConnectivityManager.current

    Scaffold(
        topBar = {
            DividoxTopAppBar(
                title = stringResource(Res.string.favorites_title),
                onBack = { onEvent(BackClicked) },
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

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
                    query = state.searchQuery,
                    onQueryChange = { onEvent(SearchQueryChanged(it)) },
                    placeholder = stringResource(Res.string.search_security_hint),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
                )
            }

            if (state.favorites.isEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.favourites_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.large),
                    )
                }
            } else {
                items(state.favorites, key = { it.entry.tickerId }) { entry ->
                    val ticker = entry.entry.tickerId
                    SecurityCard(
                        ticker = ticker,
                        companyName = entry.companyInfo?.name,
                        price = state.convertedPrices[ticker] ?: entry.quote?.price,
                        changePercent = entry.quote?.changePercent,
                        currency = state.currency,
                        isFavorite = true,
                        isInPortfolio = entry.isInPortfolio,
                        onFavoriteToggle = { onEvent(FavoriteToggled(ticker)) },
                        onClick = { onEvent(SecurityClicked(ticker)) },
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
