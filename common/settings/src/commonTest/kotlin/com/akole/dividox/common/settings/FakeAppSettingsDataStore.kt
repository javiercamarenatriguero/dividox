package com.akole.dividox.common.settings

import com.akole.dividox.common.settings.domain.datastore.AppSettingsDataStore
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.ui.resources.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeAppSettingsDataStore : AppSettingsDataStore {

    private val _settings = MutableStateFlow(AppSettings())

    val setCurrencyInvocations = mutableListOf<Currency>()

    fun emitSettings(settings: AppSettings) {
        _settings.value = settings
    }

    override fun observe(): Flow<AppSettings> = _settings

    override suspend fun setCurrency(currency: Currency) {
        setCurrencyInvocations.add(currency)
        _settings.update { it.copy(currency = currency) }
    }
}
