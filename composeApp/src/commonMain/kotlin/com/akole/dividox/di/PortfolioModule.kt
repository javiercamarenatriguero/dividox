package com.akole.dividox.di

import com.akole.dividox.component.auth.domain.repository.AuthRepository
import com.akole.dividox.component.auth.domain.usecase.GetCurrentUserIdUseCase
import com.akole.dividox.component.portfolio.data.datasource.FirestorePortfolioDataSource
import com.akole.dividox.component.portfolio.data.datasource.PortfolioDataSource
import com.akole.dividox.component.portfolio.data.repository.PortfolioRepositoryImpl
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository
import com.akole.dividox.component.portfolio.domain.usecase.AddHoldingUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.component.portfolio.domain.usecase.RemoveHoldingUseCase
import com.akole.dividox.component.portfolio.domain.usecase.UpdateHoldingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val portfolioModule: Module = module {
    single<PortfolioDataSource> {
        FirestorePortfolioDataSource(
            userIdProvider = { get<GetCurrentUserIdUseCase>()() },
            authUserIdFlow = get<StateFlow<String?>>(),
            tokenReadyProvider = { get<AuthRepository>().ensureTokenReady() },
        )
    }
    single<PortfolioRepository> {
        PortfolioRepositoryImpl(
            dataSource = get(),
            ioDispatcher = Dispatchers.Default,
        )
    }
    factoryOf(::GetPortfolioUseCase)
    factoryOf(::AddHoldingUseCase)
    factoryOf(::UpdateHoldingUseCase)
    factoryOf(::RemoveHoldingUseCase)
}
