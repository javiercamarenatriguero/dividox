package com.akole.dividox.common.settings.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.akole.dividox.common.settings.domain.datastore.AppSettingsDataStore
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.ui.resources.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class AppSettingsDataStoreImpl(
    private val dataStore: DataStore<Preferences>,
) : AppSettingsDataStore {

    private val currencyKey = stringPreferencesKey(KEY_CURRENCY)

    override fun observe(): Flow<AppSettings> =
        dataStore.data.map { prefs ->
            val currencyCode = prefs[currencyKey]
            val currency = currencyCode?.let { code ->
                Currency.entries.firstOrNull { it.code == code }
            } ?: AppSettings().currency
            AppSettings(currency = currency)
        }

    override suspend fun setCurrency(currency: Currency) {
        dataStore.edit { prefs ->
            prefs[currencyKey] = currency.code
        }
    }

    companion object {
        private const val KEY_CURRENCY = "currency"
    }
}
