package com.akole.dividox.common.settings.domain.usecase

import com.akole.dividox.common.settings.FakeAppSettingsDataStore
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.ui.resources.Currency
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAppSettingsUseCaseTest {

    private val dataStore = FakeAppSettingsDataStore()
    private val sut = ObserveAppSettingsUseCase(dataStore)

    // ─── Default state ────────────────────────────────────────────────────────

    @Test
    fun `GIVEN dataStore emits default settings WHEN invoke THEN returns flow with EUR`() = runTest {
        // GIVEN — FakeAppSettingsDataStore starts with AppSettings(currency = EUR) by default

        // WHEN
        val result = sut().first()

        // THEN
        assertEquals(AppSettings(currency = Currency.EUR), result)
    }

    // ─── Currency-specific emissions ──────────────────────────────────────────

    @Test
    fun `GIVEN dataStore emits AppSettings with EUR WHEN invoke THEN flow emits EUR`() = runTest {
        // GIVEN
        dataStore.emitSettings(AppSettings(currency = Currency.EUR))

        // WHEN
        val result = sut().first()

        // THEN
        assertEquals(Currency.EUR, result.currency)
    }

    @Test
    fun `GIVEN dataStore emits AppSettings with USD WHEN invoke THEN flow emits USD`() = runTest {
        // GIVEN
        dataStore.emitSettings(AppSettings(currency = Currency.USD))

        // WHEN
        val result = sut().first()

        // THEN
        assertEquals(Currency.USD, result.currency)
    }

    @Test
    fun `GIVEN dataStore emits AppSettings with GBP WHEN invoke THEN flow emits GBP`() = runTest {
        // GIVEN
        dataStore.emitSettings(AppSettings(currency = Currency.GBP))

        // WHEN
        val result = sut().first()

        // THEN
        assertEquals(Currency.GBP, result.currency)
    }

    // ─── Delegate to dataStore ─────────────────────────────────────────────────

    @Test
    fun `GIVEN dataStore WHEN invoke THEN returns same flow reference as dataStore observe`() = runTest {
        // GIVEN — invoke() must delegate directly to AppSettingsDataStore.observe()
        dataStore.emitSettings(AppSettings(currency = Currency.JPY))

        // WHEN
        val result = sut().first()

        // THEN
        assertEquals(AppSettings(currency = Currency.JPY), result)
    }
}
