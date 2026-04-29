package com.akole.dividox.common.settings.domain.datastore

import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.ui.resources.Currency
import kotlinx.coroutines.flow.Flow

interface AppSettingsDataStore {
    fun observe(): Flow<AppSettings>
    suspend fun setCurrency(currency: Currency)
}
