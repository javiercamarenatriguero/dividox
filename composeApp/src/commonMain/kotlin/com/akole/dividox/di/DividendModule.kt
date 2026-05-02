package com.akole.dividox.di

import com.akole.dividox.component.dividend.data.datasource.DividendLocalDataSource
import com.akole.dividox.component.dividend.data.datasource.DividendRemoteDataSource
import com.akole.dividox.component.dividend.data.db.buildDividendLocalDataSource
import com.akole.dividox.component.dividend.data.repository.DividendRepositoryImpl
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.component.dividend.domain.usecase.AddDividendPaymentUseCase
import com.akole.dividox.component.dividend.domain.usecase.GetDividendHistoryUseCase
import com.akole.dividox.component.dividend.domain.usecase.GetLifetimeDividendsUseCase
import com.akole.dividox.component.dividend.domain.usecase.GetUpcomingPaymentsUseCase
import com.akole.dividox.component.dividend.domain.usecase.GetYtdDividendsUseCase
import com.akole.dividox.component.auth.domain.usecase.GetCurrentUserIdUseCase
import org.koin.dsl.module

/**
 * Koin module for the dividend component.
 *
 * Wiring:
 * 1. [DividendDatabase] — created via platform-specific [buildDividendDatabase].
 * 2. [DividendLocalDataSource] — wraps the Room DAO.
 * 3. [DividendRemoteDataSource] — wraps Firestore, scoped to the current user.
 * 4. [DividendRepository] — [DividendRepositoryImpl] bridging local + remote.
 * 5. Use cases — one factory per use case.
 */
val dividendModule = module {
    single<DividendLocalDataSource> {
        buildDividendLocalDataSource()
    }

    single {
        DividendRemoteDataSource(userId = get<GetCurrentUserIdUseCase>()())
    }

    single<DividendRepository> {
        DividendRepositoryImpl(get(), get())
    }

    factory { GetDividendHistoryUseCase(get()) }
    factory { GetLifetimeDividendsUseCase(get()) }
    factory { GetYtdDividendsUseCase(get()) }
    factory { GetUpcomingPaymentsUseCase(get()) }
    factory { AddDividendPaymentUseCase(get()) }
}
