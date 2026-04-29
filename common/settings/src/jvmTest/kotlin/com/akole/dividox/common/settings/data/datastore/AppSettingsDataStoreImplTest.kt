package com.akole.dividox.common.settings.data.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.ui.resources.Currency
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppSettingsDataStoreImplTest {

    private val testFile: File = File(
        System.getProperty("java.io.tmpdir"),
        "test_app_settings_${System.nanoTime()}.preferences_pb",
    )

    private val dataStore = createDataStore { testFile.absolutePath }
    private val sut = AppSettingsDataStoreImpl(dataStore)

    @AfterTest
    fun tearDown() {
        testFile.delete()
    }

    // ─── Defaults ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN no stored currency WHEN observe THEN emits AppSettings with default EUR`() = runTest {
        // GIVEN — fresh DataStore, no value written

        // WHEN
        val result = sut.observe().first()

        // THEN
        assertEquals(AppSettings(currency = Currency.EUR), result)
    }

    // ─── setCurrency → observe ────────────────────────────────────────────────

    @Test
    fun `GIVEN setCurrency USD WHEN observe THEN emits AppSettings with USD`() = runTest {
        // GIVEN
        sut.setCurrency(Currency.USD)

        // WHEN
        val result = sut.observe().first()

        // THEN
        assertEquals(AppSettings(currency = Currency.USD), result)
    }

    @Test
    fun `GIVEN setCurrency EUR WHEN observe THEN emits AppSettings with EUR`() = runTest {
        // GIVEN
        sut.setCurrency(Currency.EUR)

        // WHEN
        val result = sut.observe().first()

        // THEN
        assertEquals(AppSettings(currency = Currency.EUR), result)
    }

    @Test
    fun `GIVEN setCurrency GBP WHEN observe THEN emits AppSettings with GBP`() = runTest {
        // GIVEN
        sut.setCurrency(Currency.GBP)

        // WHEN
        val result = sut.observe().first()

        // THEN
        assertEquals(AppSettings(currency = Currency.GBP), result)
    }

    // ─── Overwrite ────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN currency EUR WHEN setCurrency USD THEN observe emits AppSettings with USD`() = runTest {
        // GIVEN
        sut.setCurrency(Currency.EUR)

        // WHEN
        sut.setCurrency(Currency.USD)

        // THEN
        val result = sut.observe().first()
        assertEquals(AppSettings(currency = Currency.USD), result)
    }

    @Test
    fun `GIVEN currency USD WHEN setCurrency EUR THEN observe emits AppSettings with EUR`() = runTest {
        // GIVEN
        sut.setCurrency(Currency.USD)

        // WHEN
        sut.setCurrency(Currency.EUR)

        // THEN
        val result = sut.observe().first()
        assertEquals(AppSettings(currency = Currency.EUR), result)
    }

    // ─── Unknown stored value ─────────────────────────────────────────────────

    @Test
    fun `GIVEN unknown stored currency code WHEN observe THEN falls back to default EUR`() = runTest {
        // GIVEN — write an unrecognised code directly into the underlying preferences
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("currency")] = "UNKNOWN_CURRENCY_XYZ"
        }

        // WHEN
        val result = sut.observe().first()

        // THEN — implementation falls back to AppSettings().currency = EUR
        assertEquals(AppSettings(currency = Currency.EUR), result)
    }
}
