package com.akole.dividox.common.settings.di

import com.akole.dividox.common.settings.data.datastore.AppSettingsDataStoreImpl
import com.akole.dividox.common.settings.data.datastore.createDataStore
import com.akole.dividox.common.settings.domain.datastore.AppSettingsDataStore
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.common.settings.domain.usecase.SetCurrencyUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val settingsModule: Module = module {
    single { createDataStore() }
    single<AppSettingsDataStore> { AppSettingsDataStoreImpl(get()) }
    factoryOf(::ObserveAppSettingsUseCase)
    factoryOf(::SetCurrencyUseCase)
}
