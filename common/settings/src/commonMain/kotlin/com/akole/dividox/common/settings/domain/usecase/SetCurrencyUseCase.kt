package com.akole.dividox.common.settings.domain.usecase

import com.akole.dividox.common.settings.domain.datastore.AppSettingsDataStore
import com.akole.dividox.common.ui.resources.Currency

class SetCurrencyUseCase(private val dataStore: AppSettingsDataStore) {
    suspend operator fun invoke(currency: Currency) = dataStore.setCurrency(currency)
}
