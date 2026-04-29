package com.akole.dividox.di

import com.akole.dividox.common.settings.data.datastore.AppSettingsDataStoreImpl
import com.akole.dividox.common.settings.data.datastore.createDataStore
import com.akole.dividox.common.settings.domain.datastore.AppSettingsDataStore
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.common.settings.domain.usecase.SetCurrencyUseCase
import org.koin.dsl.module

internal expect fun dataStorePath(): String

val settingsModule = module {
    single { createDataStore(::dataStorePath) }
    single<AppSettingsDataStore> { AppSettingsDataStoreImpl(get()) }
    factory { ObserveAppSettingsUseCase(get()) }
    factory { SetCurrencyUseCase(get()) }
}
