package com.akole.dividox.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

object KoinInitializer {

    fun init(config: KoinApplication.() -> Unit = {}) {
        startKoin {
            config()
            modules(
                appModule,
                currencyModule,
                portfolioModule,
                marketModule,
                watchlistModule,
                securityIntegrationModule,
                settingsModule,
                dividendModule,
                connectivityModule,
                viewModelModule,
            )
        }
    }
}
