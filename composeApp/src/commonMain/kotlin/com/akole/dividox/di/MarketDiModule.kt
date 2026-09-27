package com.akole.dividox.di

import com.akole.dividox.common.network.HttpClientConfig
import com.akole.dividox.common.network.HttpClientFactory
import com.akole.dividox.component.dividend.data.db.DividendDatabase
import com.akole.dividox.component.market.data.datasource.CompanyInfoLocalDataSource
import com.akole.dividox.component.market.data.datasource.DividendInfoLocalDataSource
import com.akole.dividox.component.market.data.datasource.NewsLocalDataSource
import com.akole.dividox.component.market.data.datasource.StockQuoteLocalDataSource
import com.akole.dividox.component.market.data.repository.MarketRepositoryImpl
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.market.RoomCompanyInfoLocalDataSource
import com.akole.dividox.market.RoomDividendInfoLocalDataSource
import com.akole.dividox.market.RoomNewsLocalDataSource
import com.akole.dividox.market.RoomStockQuoteLocalDataSource
import com.akole.dividox.component.market.domain.usecase.GetCompanyInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetDividendHistoryUseCase
import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetHistoricalDividendEventsUseCase
import com.akole.dividox.component.market.domain.usecase.GetMajorMarketIndicesUseCase
import com.akole.dividox.component.market.domain.usecase.GetMarketNewsUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.market.domain.usecase.GetStockNewsUseCase
import com.akole.dividox.component.market.domain.usecase.GetPriceHistoryUseCase
import com.akole.dividox.component.market.domain.usecase.GetStockQuoteUseCase
import com.akole.dividox.component.market.domain.usecase.SearchSecuritiesUseCase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val marketModule: Module = module {
    single {
        HttpClientFactory(
            HttpClientConfig(
                timeoutMs = 10_000,
                defaultHeaders = mapOf("User-Agent" to "Mozilla/5.0"),
            ),
        ).build()
    }
    single<StockQuoteLocalDataSource> {
        RoomStockQuoteLocalDataSource(get<DividendDatabase>().stockQuoteDao())
    }
    single<CompanyInfoLocalDataSource> {
        RoomCompanyInfoLocalDataSource(get<DividendDatabase>().companyInfoDao())
    }
    single<NewsLocalDataSource> {
        RoomNewsLocalDataSource(get<DividendDatabase>().newsDao())
    }
    single<DividendInfoLocalDataSource> {
        RoomDividendInfoLocalDataSource(get<DividendDatabase>().dividendInfoDao())
    }
    single<MarketRepository> {
        MarketRepositoryImpl(
            httpClient = get(),
            ioDispatcher = Dispatchers.Default,
            localDataSource = get(),
            companyInfoLocalDataSource = get(),
            newsLocalDataSource = get(),
            dividendInfoLocalDataSource = get(),
        )
    }
    factoryOf(::GetStockQuoteUseCase)
    factoryOf(::GetMultipleQuotesUseCase)
    factoryOf(::GetDividendInfoUseCase)
    factoryOf(::GetCompanyInfoUseCase)
    factoryOf(::GetDividendHistoryUseCase)
    factoryOf(::GetHistoricalDividendEventsUseCase)
    factoryOf(::GetPriceHistoryUseCase)
    factoryOf(::SearchSecuritiesUseCase)
    factoryOf(::GetMajorMarketIndicesUseCase)

    factoryOf(::GetMarketNewsUseCase)
    factoryOf(::GetStockNewsUseCase)
}
