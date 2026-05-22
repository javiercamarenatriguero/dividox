package com.akole.dividox.di

import com.akole.dividox.getAppVersion
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordViewModel
import com.akole.dividox.feature.auth.login.LoginViewModel
import com.akole.dividox.feature.auth.register.SignUpViewModel
import com.akole.dividox.feature.analysis.SecurityDetailViewModel
import com.akole.dividox.feature.dividends.DividendsViewModel
import com.akole.dividox.feature.favorites.FavoritesViewModel
import com.akole.dividox.feature.search.SearchViewModel
import com.akole.dividox.feature.dashboard.DashboardViewModel
import com.akole.dividox.feature.home.HomeViewModel
import com.akole.dividox.feature.onboarding.OnboardingViewModel
import com.akole.dividox.feature.settings.DeleteAccountUseCase
import com.akole.dividox.feature.settings.SettingsViewModel
import com.akole.dividox.feature.portfolio.HoldingViewModel
import com.akole.dividox.feature.portfolio.PortfolioViewModel
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.common.ui.resources.di.getCurrentTimeMillis
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel { params ->
        HomeViewModel(
            greeting = params.get(),
            platformName = params.get(),
        )
    }
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModelOf(::SignUpViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::DividendsViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::SearchViewModel)
    factory { DeleteAccountUseCase(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), getAppVersion()) }
    viewModel { PortfolioViewModel(get(), get(), get(), get()) }

    // SecurityDetailViewModel with required ticker parameter
    viewModel { params ->
        SecurityDetailViewModel(
            ticker = params.get(),
            getSecurityDetail = get(),
            getStockQuote = get(),
            getHistoricalDividendEvents = get(),
            getPriceHistory = get(),
            isInWatchlist = get(),
            addToWatchlist = get(),
            removeFromWatchlist = get(),
            connectivityManager = get(),
            observeAppSettings = get(),
            currencyConverter = get(),
            refreshTracker = get(),
            getStockNews = get(),
        )
    }

    // HoldingViewModel with optional holdingId parameter for Add/Edit modes
    viewModel { params ->
        val holdingIdValue: String? = params.component1()
        val prefillTicker: String? = if (params.size() > 1) params.component2() else null
        val holdingId = holdingIdValue?.let { HoldingId(it) }
        HoldingViewModel(
            holdingId = holdingId,
            prefillTicker = prefillTicker,
            searchSecurities = get(),
            getStockQuote = get(),
            addHolding = get(),
            updateHolding = get(),
            removeHolding = get(),
            getPortfolio = get(),
            getCurrentTimeMillis = { getCurrentTimeMillis() },
            observeAppSettings = get(),
        )
    }
}
