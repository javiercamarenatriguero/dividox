package com.akole.dividox.di

import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordViewModel
import com.akole.dividox.feature.auth.login.LoginViewModel
import com.akole.dividox.feature.auth.register.SignUpViewModel
import com.akole.dividox.feature.dashboard.DashboardViewModel
import com.akole.dividox.feature.home.HomeViewModel
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
    viewModelOf(::DashboardViewModel)
    viewModelOf(::PortfolioViewModel)
    
    // HoldingViewModel with optional holdingId parameter for Add/Edit modes
    viewModel { params ->
        val holdingIdValue: String? = params.getOrNull()
        val holdingId = holdingIdValue?.let { HoldingId(it) }
        HoldingViewModel(
            holdingId = holdingId,
            searchSecurities = get(),
            addHolding = get(),
            updateHolding = get(),
            removeHolding = get(),
            getCurrentTimeMillis = { getCurrentTimeMillis() },
            observeAppSettings = get(),
        )
    }
}
