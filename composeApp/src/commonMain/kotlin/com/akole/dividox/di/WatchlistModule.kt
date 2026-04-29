package com.akole.dividox.di

import com.akole.dividox.component.auth.domain.usecase.GetCurrentUserIdUseCase
import com.akole.dividox.component.watchlist.data.datasource.WatchlistDataSource
import com.akole.dividox.component.watchlist.data.datasource.WatchlistFirestoreDataSource
import com.akole.dividox.component.watchlist.data.repository.WatchlistRepositoryImpl
import com.akole.dividox.component.watchlist.domain.repository.WatchlistRepository
import com.akole.dividox.component.watchlist.domain.usecase.AddToWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.GetWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.IsInWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val watchlistModule: Module = module {
    single<WatchlistDataSource> {
        WatchlistFirestoreDataSource(userId = get<GetCurrentUserIdUseCase>()())
    }
    single<WatchlistRepository> {
        WatchlistRepositoryImpl(
            dataSource = get(),
            ioDispatcher = Dispatchers.Default,
        )
    }
    factoryOf(::GetWatchlistUseCase)
    factoryOf(::AddToWatchlistUseCase)
    factoryOf(::RemoveFromWatchlistUseCase)
    factoryOf(::IsInWatchlistUseCase)
}
