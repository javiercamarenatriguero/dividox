package com.akole.dividox.feature.analysis

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.AppRefreshTracker
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.usecase.GetStockQuoteUseCase
import com.akole.dividox.component.watchlist.domain.usecase.AddToWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.IsInWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.feature.analysis.SecurityDetailContract.SecurityDetailViewEvent
import com.akole.dividox.feature.analysis.SecurityDetailContract.SecurityDetailSideEffect
import com.akole.dividox.integration.security.domain.model.SecurityDetail
import com.akole.dividox.integration.security.domain.usecase.GetSecurityDetailUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Clock
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SecurityDetailViewModelTest {

    private val ticker = "AAPL"
    private val mockGetSecurityDetail = mockk<GetSecurityDetailUseCase>()
    private val mockGetStockQuote = mockk<GetStockQuoteUseCase>()
    private val mockIsInWatchlist = mockk<IsInWatchlistUseCase>()
    private val mockAddToWatchlist = mockk<AddToWatchlistUseCase>()
    private val mockRemoveFromWatchlist = mockk<RemoveFromWatchlistUseCase>()
    private val mockConnectivityManager = mockk<NetworkConnectivityManager>()
    private val mockObserveAppSettings = mockk<ObserveAppSettingsUseCase>()
    private val mockCurrencyConverter = mockk<CurrencyConverter>()
    private val mockRefreshTracker = mockk<AppRefreshTracker>()

    private fun createViewModel(): SecurityDetailViewModel {
        // Setup default mock behaviors
        every { mockObserveAppSettings() } returns flowOf(
            AppSettings(currency = Currency.USD)
        )
        every { mockIsInWatchlist(any()) } returns flowOf(false)
        coEvery { mockGetStockQuote(any()).getOrNull() } returns null
        every { mockConnectivityManager.isConnected } returns flowOf(true)

        return SecurityDetailViewModel(
            ticker = ticker,
            getSecurityDetail = mockGetSecurityDetail,
            getStockQuote = mockGetStockQuote,
            isInWatchlist = mockIsInWatchlist,
            addToWatchlist = mockAddToWatchlist,
            removeFromWatchlist = mockRemoveFromWatchlist,
            connectivityManager = mockConnectivityManager,
            observeAppSettings = mockObserveAppSettings,
            currencyConverter = mockCurrencyConverter,
            refreshTracker = mockRefreshTracker,
        )
    }

    @Test
    fun `SHOULD load security details on OnLoad GIVEN initial state`() = runTest {
        // GIVEN
        val quote = StockQuote(
            ticker = ticker,
            price = 150.0,
            change = 2.5,
            changePercent = 1.69,
            currency = "USD",
            lastUpdated = Clock.System.now(),
        )
        val priceHistory = listOf(
            PricePoint(Clock.System.now(), 150.0),
            PricePoint(Clock.System.now(), 149.5),
        )
        val securityDetail = SecurityDetail(
            ticker = ticker,
            quote = quote,
            dividendInfo = null,
            companyInfo = null,
            priceHistory = priceHistory,
            isInPortfolio = false,
            isInWatchlist = false,
            holdingId = null,
        )

        every { mockGetSecurityDetail(ticker, ChartPeriod.ONE_YEAR) } returns flowOf(
            securityDetail
        )

        // WHEN
        val viewModel = createViewModel()
        viewModel.onViewEvent(SecurityDetailViewEvent.OnLoad)

        // THEN
        val state = viewModel.viewState.value
        assertEquals(ticker, state.ticker)
        assertEquals(quote.price, state.quote?.price)
        assertEquals(priceHistory.size, state.priceHistory.size)
    }

    @Test
    fun `SHOULD toggle favorite status WHEN OnFavoriteToggled GIVEN not in watchlist`() = runTest {
        // GIVEN
        val quote = StockQuote(
            ticker = ticker,
            price = 150.0,
            change = 2.5,
            changePercent = 1.69,
            currency = "USD",
            lastUpdated = Clock.System.now(),
        )
        every { mockGetSecurityDetail(ticker, any()) } returns flowOf(
            SecurityDetail(
                ticker = ticker,
                quote = quote,
                dividendInfo = null,
                companyInfo = null,
                priceHistory = emptyList(),
                isInPortfolio = false,
                isInWatchlist = false,
                holdingId = null,
            )
        )
        coEvery { mockAddToWatchlist(ticker) } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.onViewEvent(SecurityDetailViewEvent.OnLoad)

        // WHEN
        viewModel.onViewEvent(SecurityDetailViewEvent.OnFavoriteToggled)

        // THEN
        coVerify { mockAddToWatchlist(ticker) }
    }

    @Test
    fun `SHOULD change chart period WHEN ChartPeriodSelected GIVEN valid period`() = runTest {
        // GIVEN
        every { mockGetSecurityDetail(ticker, ChartPeriod.ONE_WEEK) } returns flowOf(
            SecurityDetail(
                ticker = ticker,
                quote = StockQuote(
                    ticker = ticker,
                    price = 150.0,
                    change = 0.5,
                    changePercent = 0.33,
                    currency = "USD",
                    lastUpdated = Clock.System.now(),
                ),
                dividendInfo = null,
                companyInfo = null,
                priceHistory = emptyList(),
                isInPortfolio = false,
                isInWatchlist = false,
                holdingId = null,
            )
        )

        val viewModel = createViewModel()

        // WHEN
        viewModel.onViewEvent(SecurityDetailViewEvent.ChartPeriodSelected(ChartPeriod.ONE_WEEK))

        // THEN
        val state = viewModel.viewState.value
        assertEquals(ChartPeriod.ONE_WEEK, state.selectedChartPeriod)
    }

    @Test
    fun `SHOULD toggle dividend chart mode WHEN ToggleDividendChartMode GIVEN current state`() = runTest {
        // GIVEN
        every { mockGetSecurityDetail(ticker, any()) } returns emptyFlow()
        val viewModel = createViewModel()
        val initialMode = viewModel.viewState.value.isDividendChartPercentage

        // WHEN
        viewModel.onViewEvent(SecurityDetailViewEvent.ToggleDividendChartMode)

        // THEN
        val state = viewModel.viewState.value
        assertEquals(!initialMode, state.isDividendChartPercentage)
    }

    @Test
    fun `SHOULD emit navigation side effect WHEN OnBackClicked GIVEN in detail view`() = runTest {
        // GIVEN
        every { mockGetSecurityDetail(ticker, any()) } returns emptyFlow()
        val viewModel = createViewModel()

        // WHEN
        viewModel.onViewEvent(SecurityDetailViewEvent.OnBackClicked)

        // THEN
        val sideEffect = viewModel.sideEffect.value
        assertEquals(SecurityDetailSideEffect.Navigation.NavigateBack, sideEffect)
    }
}
