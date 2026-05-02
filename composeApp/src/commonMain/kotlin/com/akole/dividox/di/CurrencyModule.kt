package com.akole.dividox.di

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.data.datastore.createExchangeRateDataStore
import com.akole.dividox.common.currency.data.datasource.DataStoreExchangeRateLocalDataSource
import com.akole.dividox.common.currency.data.datasource.ExchangeRateDataSource
import com.akole.dividox.common.currency.data.datasource.FrankfurterExchangeRateDataSource
import com.akole.dividox.common.currency.data.datasource.LocalExchangeRateDataSource
import com.akole.dividox.common.currency.data.repository.ExchangeRateRepositoryImpl
import com.akole.dividox.common.currency.domain.repository.ExchangeRateRepository
import com.akole.dividox.common.currency.domain.usecase.GetExchangeRatesUseCase
import com.akole.dividox.common.network.HttpClientConfig
import com.akole.dividox.common.network.HttpClientFactory
import com.akole.dividox.common.ui.resources.di.todayIn
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal expect fun exchangeRatePath(): String

val currencyModule: Module = module {
    single<ExchangeRateDataSource> {
        FrankfurterExchangeRateDataSource(
            httpClient = HttpClientFactory(
                HttpClientConfig(timeoutMs = 10_000, defaultHeaders = emptyMap()),
            ).build(),
        )
    }
    single<LocalExchangeRateDataSource> {
        DataStoreExchangeRateLocalDataSource(
            dataStore = createExchangeRateDataStore(::exchangeRatePath),
        )
    }
    single<ExchangeRateRepository> {
        ExchangeRateRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            ioDispatcher = Dispatchers.Default,
            todayProvider = { todayIn(TimeZone.UTC) },
        )
    }
    factoryOf(::GetExchangeRatesUseCase)
    single { CurrencyConverter(get()) }
}
