package com.akole.dividox.di

import com.akole.dividox.common.settings.di.settingsModule
import com.akole.dividox.integration.security.di.securityIntegrationModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

object KoinInitializer {

    fun init(config: KoinApplication.() -> Unit = {}) {
        startKoin {
            config()
            modules(
                appModule,
                portfolioModule,
                marketModule,
                watchlistModule,
                securityIntegrationModule,
                settingsModule,
                viewModelModule,
            )
        }
    }
}
