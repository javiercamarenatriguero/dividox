package com.akole.dividox.common.settings.domain.usecase

import com.akole.dividox.common.settings.domain.datastore.AppSettingsDataStore

class SetDefaultMarketUseCase(private val dataStore: AppSettingsDataStore) {
    suspend operator fun invoke(market: String) = dataStore.setDefaultMarket(market)
}
