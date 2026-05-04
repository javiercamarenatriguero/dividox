package com.akole.dividox.di

import com.akole.dividox.integration.dividend.domain.usecase.GetDividendActivitySummaryUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetDividendProjectionBarsUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetEnrichedPaymentHistoryUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetEnrichedUpcomingPaymentsUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetPeriodDividendsUseCase
import com.akole.dividox.integration.dividend.domain.usecase.ObservePortfolioChangesUseCase
import com.akole.dividox.integration.dividend.domain.usecase.SyncDividendHistoryFromHoldingsUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dividendIntegrationModule: Module = module {
    factoryOf(::GetDividendActivitySummaryUseCase)
    factoryOf(::GetEnrichedUpcomingPaymentsUseCase)
    factoryOf(::GetEnrichedPaymentHistoryUseCase)
    factoryOf(::ObservePortfolioChangesUseCase)
    factoryOf(::SyncDividendHistoryFromHoldingsUseCase)
    factoryOf(::GetPeriodDividendsUseCase)
    factory { GetDividendProjectionBarsUseCase(dividendRepository = get()) }
}
