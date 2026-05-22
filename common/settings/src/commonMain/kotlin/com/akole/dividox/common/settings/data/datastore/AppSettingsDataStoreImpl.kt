package com.akole.dividox.common.settings.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.akole.dividox.common.settings.domain.datastore.AppSettingsDataStore
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.currency.domain.model.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppSettingsDataStoreImpl(
    private val dataStore: DataStore<Preferences>,
) : AppSettingsDataStore {

    private val currencyKey = stringPreferencesKey(KEY_CURRENCY)
    private val biometricLockKey = booleanPreferencesKey(KEY_BIOMETRIC_LOCK)
    private val defaultMarketKey = stringPreferencesKey(KEY_DEFAULT_MARKET)
    private val onboardingCompletedKey = booleanPreferencesKey(KEY_ONBOARDING_COMPLETED)

    override fun observe(): Flow<AppSettings> =
        dataStore.data.map { prefs ->
            val currencyCode = prefs[currencyKey]
            val currency = currencyCode?.let { code ->
                Currency.entries.firstOrNull { it.code == code }
            } ?: AppSettings().currency
            val biometricLockEnabled = prefs[biometricLockKey] ?: false
            val defaultMarket = prefs[defaultMarketKey] ?: AppSettings().defaultMarket
            val onboardingCompleted = prefs[onboardingCompletedKey] ?: false
            AppSettings(
                currency = currency,
                biometricLockEnabled = biometricLockEnabled,
                defaultMarket = defaultMarket,
                onboardingCompleted = onboardingCompleted,
            )
        }

    override suspend fun setCurrency(currency: Currency) {
        dataStore.edit { prefs ->
            prefs[currencyKey] = currency.code
        }
    }

    override suspend fun setBiometricLock(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[biometricLockKey] = enabled
        }
    }

    override suspend fun setDefaultMarket(market: String) {
        dataStore.edit { prefs ->
            prefs[defaultMarketKey] = market
        }
    }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { prefs ->
            prefs[onboardingCompletedKey] = true
        }
    }

    companion object {
        private const val KEY_CURRENCY = "currency"
        private const val KEY_BIOMETRIC_LOCK = "biometric_lock_enabled"
        private const val KEY_DEFAULT_MARKET = "default_market"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
