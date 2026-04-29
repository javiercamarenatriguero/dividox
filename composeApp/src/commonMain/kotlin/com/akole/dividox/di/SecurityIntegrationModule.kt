package com.akole.dividox.di

import com.akole.dividox.integration.security.domain.usecase.GetEnrichedWatchlistUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioSummaryUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import com.akole.dividox.integration.security.domain.usecase.GetSecurityDetailUseCase
import com.akole.dividox.integration.security.domain.usecase.GetSecurityHoldingUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val securityIntegrationModule: Module = module {
    factoryOf(::GetPortfolioWithQuotesUseCase)
    factoryOf(::GetPortfolioSummaryUseCase)
    factoryOf(::GetEnrichedWatchlistUseCase)
    factoryOf(::GetSecurityDetailUseCase)
    factoryOf(::GetSecurityHoldingUseCase)
}
