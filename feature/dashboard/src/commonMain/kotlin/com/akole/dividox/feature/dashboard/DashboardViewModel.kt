package com.akole.dividox.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.common.settings.domain.usecase.SetCurrencyUseCase
import com.akole.dividox.common.ui.resources.Currency
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardSideEffect
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.CurrencyToggled
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.FavouriteToggled
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.PeriodSelected
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.SecurityClicked
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.ViewAllFavouritesClicked
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewState
import com.akole.dividox.integration.security.domain.usecase.GetEnrichedWatchlistUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioSummaryUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getPortfolioSummary: GetPortfolioSummaryUseCase,
    private val getEnrichedWatchlist: GetEnrichedWatchlistUseCase,
    private val removeFromWatchlist: RemoveFromWatchlistUseCase,
    private val observeAppSettings: ObserveAppSettingsUseCase,
    private val setCurrency: SetCurrencyUseCase,
) : ViewModel(),
    MVI<DashboardViewState, DashboardViewEvent, DashboardSideEffect> by mvi(DashboardViewState()) {

    init {
        observeData()
        observeSettings()
    }

    override fun onViewEvent(viewEvent: DashboardViewEvent) {
        when (viewEvent) {
            is PeriodSelected -> updateViewState { copy(selectedPeriod = viewEvent.period) }
            CurrencyToggled -> toggleCurrency()
            is FavouriteToggled -> removeFavourite(viewEvent.ticker)
            is SecurityClicked -> viewModelScope.emitSideEffect(
                DashboardSideEffect.Navigation.NavigateToSecurity(viewEvent.ticker),
            )
            ViewAllFavouritesClicked -> viewModelScope.emitSideEffect(
                DashboardSideEffect.Navigation.NavigateToFavorites,
            )
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                getPortfolioSummary(),
                getEnrichedWatchlist(),
            ) { summary, watchlist ->
                viewState.value.copy(
                    isLoading = false,
                    summary = summary,
                    watchlist = watchlist,
                    error = null,
                )
            }.collect { newState ->
                updateViewState { newState }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            observeAppSettings().collect { settings ->
                updateViewState { copy(currency = settings.currency) }
            }
        }
    }

    private fun toggleCurrency() {
        viewModelScope.launch {
            val next = if (viewState.value.currency == Currency.EUR) Currency.USD else Currency.EUR
            setCurrency(next)
        }
    }

    private fun removeFavourite(ticker: String) {
        viewModelScope.launch {
            removeFromWatchlist(ticker)
        }
    }
}
