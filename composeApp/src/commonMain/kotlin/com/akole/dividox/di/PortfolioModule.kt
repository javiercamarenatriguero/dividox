package com.akole.dividox.di

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
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin DI module for portfolio component.
 * Wires datasource, repository, and all usecases for managing user holdings.
 * Consumes [GetCurrentUserIdUseCase] from appModule to isolate data per user.
 */
val portfolioModule: Module = module {
    single<PortfolioDataSource> {
        FirestorePortfolioDataSource(userId = get<GetCurrentUserIdUseCase>()())
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
