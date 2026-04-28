package com.akole.dividox.component.market.data.di

import com.akole.dividox.component.market.data.api.YahooFinanceApi
import com.akole.dividox.component.market.data.api.createYahooFinanceClient
import com.akole.dividox.component.market.data.repository.MarketRepositoryImpl
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.component.market.domain.usecase.GetCompanyInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetDividendHistoryUseCase
import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.market.domain.usecase.GetPriceHistoryUseCase
import com.akole.dividox.component.market.domain.usecase.GetStockQuoteUseCase
import com.akole.dividox.component.market.domain.usecase.SearchSecuritiesUseCase
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val marketModule: Module = module {
    single { createYahooFinanceClient() }
    single { YahooFinanceApi(get()) }
    single<MarketRepository> {
        MarketRepositoryImpl(
            api = get(),
            ioDispatcher = Dispatchers.Default,
        )
    }
    factoryOf(::GetStockQuoteUseCase)
    factoryOf(::GetMultipleQuotesUseCase)
    factoryOf(::GetDividendInfoUseCase)
    factoryOf(::GetCompanyInfoUseCase)
    factoryOf(::GetDividendHistoryUseCase)
    factoryOf(::GetPriceHistoryUseCase)
    factoryOf(::SearchSecuritiesUseCase)
}
