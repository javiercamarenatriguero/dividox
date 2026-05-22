package com.akole.dividox.di

import com.akole.dividox.common.settings.AppRefreshTracker
import com.akole.dividox.common.settings.data.biometric.BiometricAuthenticator
import com.akole.dividox.common.settings.data.share.FileShareService
import com.akole.dividox.common.settings.data.datastore.AppSettingsDataStoreImpl
import com.akole.dividox.common.settings.data.datastore.createDataStore
import com.akole.dividox.common.settings.domain.datastore.AppSettingsDataStore
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.common.settings.domain.usecase.SetCurrencyUseCase
import com.akole.dividox.common.settings.domain.usecase.SetDefaultMarketUseCase
import com.akole.dividox.common.settings.domain.usecase.SetOnboardingCompletedUseCase
import com.akole.dividox.common.settings.domain.usecase.UpdateBiometricLockUseCase
import org.koin.dsl.module

internal expect fun dataStorePath(): String

val settingsModule = module {
    single { createDataStore(::dataStorePath) }
    single<AppSettingsDataStore> { AppSettingsDataStoreImpl(get()) }
    single { AppRefreshTracker() }
    single { BiometricAuthenticator() }
    single { FileShareService() }
    factory { ObserveAppSettingsUseCase(get()) }
    factory { SetCurrencyUseCase(get()) }
    factory { SetDefaultMarketUseCase(get()) }
    factory { UpdateBiometricLockUseCase(get(), get()) }
    factory { SetOnboardingCompletedUseCase(get()) }
}
