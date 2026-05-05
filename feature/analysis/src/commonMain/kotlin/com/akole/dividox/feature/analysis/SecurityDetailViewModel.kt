package com.akole.dividox.feature.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.AppRefreshTracker
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.component.market.domain.usecase.GetHistoricalDividendEventsUseCase
import com.akole.dividox.component.market.domain.usecase.GetPriceHistoryUseCase
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class SecurityDetailViewModel(
    val ticker: String,
    private val getSecurityDetail: GetSecurityDetailUseCase,
    private val getStockQuote: GetStockQuoteUseCase,
    private val getHistoricalDividendEvents: GetHistoricalDividendEventsUseCase,
    private val getPriceHistory: GetPriceHistoryUseCase,
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
        loadHistoricalDividendGrowth()
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
                            renderedChartPeriod = period,
                            isInPortfolio = detail.isInPortfolio,
                            lastUpdated = Clock.System.now(),
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun loadHistoricalDividendGrowth() {
        viewModelScope.launch {
            runCatching {
                coroutineScope {
                    val eventsDeferred = async {
                        getHistoricalDividendEvents(ticker, DividendHistoryRange.MAX).getOrNull()
                    }
                    val pricePointsDeferred = async {
                        getPriceHistory(ticker, ChartPeriod.ALL)
                            .catch { /* ignore; bars without price data are skipped */ }
                            .first()
                    }
                    val events = eventsDeferred.await() ?: return@coroutineScope
                    val pricePoints = pricePointsDeferred.await()

                    val dividendsByYear = events
                        .groupBy { it.exDividendDate.year }
                        .mapValues { (_, evts) -> evts.sumOf { it.amountPerShare } }

                    val priceByYear = pricePoints
                        .groupBy { it.timestamp.toLocalDateTime(TimeZone.UTC).year }
                        .mapValues { (_, pts) ->
                            pts.maxByOrNull { it.timestamp }?.close ?: 0.0
                        }

                    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.UTC).year
                    val bars = (0..9).mapNotNull { yearOffset ->
                        val year = currentYear - yearOffset
                        val payout = dividendsByYear[year] ?: return@mapNotNull null
                        if (payout <= 0.0) return@mapNotNull null
                        val price = priceByYear[year]
                            ?: priceByYear.entries.filter { it.key <= year }
                                .maxByOrNull { it.key }?.value
                            ?: return@mapNotNull null
                        if (price <= 0.0) return@mapNotNull null
                        DividendGrowthBar(
                            year = year,
                            absoluteValue = payout,
                            percentageOfPrice = (payout / price) * 100,
                        )
                    }.reversed()

                    updateViewState { copy(dividendGrowthData = bars) }
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

    private fun navigateToAddSecurity() {
        viewModelScope.emitSideEffect(
            SecurityDetailSideEffect.Navigation.NavigateToAddSecurity(ticker)
        )
    }
}
