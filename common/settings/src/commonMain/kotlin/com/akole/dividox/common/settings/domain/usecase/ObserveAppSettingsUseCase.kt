package com.akole.dividox.common.settings.domain.usecase

import com.akole.dividox.common.settings.domain.datastore.AppSettingsDataStore
import com.akole.dividox.common.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

class ObserveAppSettingsUseCase(private val dataStore: AppSettingsDataStore) {
    operator fun invoke(): Flow<AppSettings> = dataStore.observe()
}
