package com.akole.dividox.common.settings.domain.usecase

import com.akole.dividox.common.settings.domain.datastore.AppSettingsDataStore

class SetOnboardingCompletedUseCase(private val dataStore: AppSettingsDataStore) {
    suspend operator fun invoke() = dataStore.setOnboardingCompleted()
}
