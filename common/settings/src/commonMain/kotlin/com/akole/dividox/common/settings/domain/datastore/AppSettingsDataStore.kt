package com.akole.dividox.common.settings.domain.datastore

import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.currency.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface AppSettingsDataStore {
    fun observe(): Flow<AppSettings>
    suspend fun setCurrency(currency: Currency)
    suspend fun setBiometricLock(enabled: Boolean)
}
