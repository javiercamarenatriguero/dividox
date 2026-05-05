package com.akole.dividox.feature.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.AppRefreshTracker
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.usecase.GetStockQuoteUseCase
import com.akole.dividox.component.watchlist.domain.usecase.AddToWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.IsInWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.feature.analysis.SecurityDetailContract.DividendGrowthBar
import com.akole.dividox.feature.analysis.SecurityDetailContract.SecurityDetailSideEffect
import com.akole.dividox.feature.analysis.SecurityDetailContract.SecurityDetailViewEvent
import com.akole.dividox.feature.analysis.SecurityDetailContract.SecurityDetailViewState
import com.akole.dividox.integration.security.domain.usecase.GetSecurityDetailUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.time.Clock

class SecurityDetailViewModel(
    val ticker: String,
    private val getSecurityDetail: GetSecurityDetailUseCase,
    private val getStockQuote: GetStockQuoteUseCase,
    private val isInWatchlist: IsInWatchlistUseCase,
    private val addToWatchlist: AddToWatchlistUseCase,
    private val removeFromWatchlist: RemoveFromWatchlistUseCase,
    private val connectivityManager: NetworkConnectivityManager,
    private val observeAppSettings: ObserveAppSettingsUseCase,
    private val currencyConverter: CurrencyConverter,
    private val refreshTracker: AppRefreshTracker,
) : ViewModel(),
    MVI<SecurityDetailViewState, SecurityDetailViewEvent, SecurityDetailSideEffect>
    by mvi(SecurityDetailViewState(ticker = ticker)) {

    private var priceHistoryJob: Job? = null

    init {
        observeData()
    }

    override fun onViewEvent(viewEvent: SecurityDetailViewEvent) {
        when (viewEvent) {
            SecurityDetailViewEvent.OnLoad -> loadSecurityDetails()
            SecurityDetailViewEvent.OnRefresh -> refresh()
            SecurityDetailViewEvent.OnBackClicked -> navigateBack()
            SecurityDetailViewEvent.OnFavoriteToggled -> toggleFavorite()
            is SecurityDetailViewEvent.ChartPeriodSelected -> selectChartPeriod(viewEvent.period)
            SecurityDetailViewEvent.ToggleDividendChartMode -> toggleDividendChartMode()
            SecurityDetailViewEvent.OnAddSecurityClicked -> navigateToAddSecurity()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                observeAppSettings().filterNotNull(),
                isInWatchlist(ticker),
            ) { settings, isFav ->
                Pair(settings.currency, isFav)
            }
                .catch { e ->
                    updateViewState { copy(error = e.message) }
                }
                .collect { (currency, isFav) ->
                    updateViewState {
                        copy(
                            currency = currency,
                            isFavorite = isFav,
                        )
                    }
                }
        }

        loadSecurityDetails()
    }

    private fun loadSecurityDetails() {
        viewModelScope.launch {
            updateViewState { copy(isLoading = true) }
            observeSecurityDetail(viewState.value.selectedChartPeriod)
        }
    }

    private fun observeSecurityDetail(period: ChartPeriod) {
        priceHistoryJob?.cancel()
        priceHistoryJob = viewModelScope.launch {
            getSecurityDetail(ticker, period)
                .catch { e ->
                    updateViewState { copy(isLoading = false, error = e.message) }
                }
                .collect { detail ->
                    updateViewState {
                        copy(
                            quote = detail.quote,
                            companyInfo = detail.companyInfo,
                            dividendInfo = detail.dividendInfo,
                            priceHistory = detail.priceHistory,
                            isInPortfolio = detail.isInPortfolio,
                            lastUpdated = Clock.System.now(),
                            isLoading = false,
                        )
                    }
                    detail.dividendInfo?.let {
                        generateDividendGrowthBars(it.annualPayout)
                    }
                }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            updateViewState { copy(isRefreshing = true) }
            updateViewState { copy(isRefreshing = false) }
        }
    }

    private fun navigateBack() {
        viewModelScope.emitSideEffect(SecurityDetailSideEffect.Navigation.NavigateBack)
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            val current = viewState.value.isFavorite
            try {
                if (current) {
                    updateViewState { copy(isFavorite = false) }
                    viewModelScope.emitSideEffect(
                        SecurityDetailSideEffect.Message.ShowSuccess("Removed from favorites")
                    )
                } else {
                    updateViewState { copy(isFavorite = true) }
                    viewModelScope.emitSideEffect(
                        SecurityDetailSideEffect.Message.ShowSuccess("Added to favorites")
                    )
                }
            } catch (e: Exception) {
                viewModelScope.emitSideEffect(
                    SecurityDetailSideEffect.Message.ShowError("Failed to update favorite: ${e.message}")
                )
            }
        }
    }

    private fun selectChartPeriod(period: ChartPeriod) {
        updateViewState { copy(selectedChartPeriod = period) }
        observeSecurityDetail(period)
    }

    private fun toggleDividendChartMode() {
        updateViewState { copy(isDividendChartPercentage = !viewState.value.isDividendChartPercentage) }
    }

    private fun generateDividendGrowthBars(currentPayout: Double) {
        // Generate 10 years of dividend growth data (mock implementation)
        // In real implementation, this would come from the repository/API
        val currentYear = 2024
        val bars = (0..9).map { yearOffset ->
            val year = currentYear - yearOffset
            val value = currentPayout * (0.95.pow(yearOffset.toDouble()))
            DividendGrowthBar(
                year = year,
                absoluteValue = value,
                percentageOfPrice = (value / (viewState.value.quote?.price ?: 1.0)) * 100,
            )
        }.reversed()

        updateViewState { copy(dividendGrowthData = bars) }
    }

    private fun navigateToAddSecurity() {
        viewModelScope.emitSideEffect(
            SecurityDetailSideEffect.Navigation.NavigateToAddSecurity(ticker)
        )
    }
}
