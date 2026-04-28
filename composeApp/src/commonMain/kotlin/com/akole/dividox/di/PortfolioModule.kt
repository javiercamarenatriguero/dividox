package com.akole.dividox.di

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
 * Uses authenticated user's uid from Firebase Auth for data isolation.
 */
val portfolioModule: Module = module {
    single<PortfolioDataSource> {
        val userId = getCurrentUserId()
        createPortfolioDataSource(userId = userId)
    }
    single<PortfolioRepository> {
        PortfolioRepositoryImpl(
            dataSource = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }
    factoryOf(::GetPortfolioUseCase)
    factoryOf(::AddHoldingUseCase)
    factoryOf(::UpdateHoldingUseCase)
    factoryOf(::RemoveHoldingUseCase)
}

/**
 * Get current authenticated user's uid from Firebase Auth.
 * Ensures portfolio data is isolated per user.
 *
 * @return Firebase Auth uid of logged-in user
 * @throws Exception if no user is authenticated
 */
internal expect fun getCurrentUserId(): String
