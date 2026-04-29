package com.akole.dividox.common.settings.domain.usecase

import com.akole.dividox.common.settings.FakeAppSettingsDataStore
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.ui.resources.Currency
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SetCurrencyUseCaseTest {

    private val dataStore = FakeAppSettingsDataStore()
    private val sut = SetCurrencyUseCase(dataStore)

    // ─── Delegation ───────────────────────────────────────────────────────────

    @Test
    fun `WHEN invoke USD THEN dataStore setCurrency is called with USD`() = runTest {
        // WHEN
        sut(Currency.USD)

        // THEN
        assertEquals(1, dataStore.setCurrencyInvocations.size)
        assertEquals(Currency.USD, dataStore.setCurrencyInvocations.first())
    }

    @Test
    fun `WHEN invoke EUR THEN dataStore setCurrency is called with EUR`() = runTest {
        // WHEN
        sut(Currency.EUR)

        // THEN
        assertEquals(1, dataStore.setCurrencyInvocations.size)
        assertEquals(Currency.EUR, dataStore.setCurrencyInvocations.first())
    }

    @Test
    fun `WHEN invoke GBP THEN dataStore setCurrency is called with GBP`() = runTest {
        // WHEN
        sut(Currency.GBP)

        // THEN
        assertEquals(Currency.GBP, dataStore.setCurrencyInvocations.first())
    }

    // ─── State propagation ────────────────────────────────────────────────────

    @Test
    fun `GIVEN initial EUR WHEN invoke USD THEN observe emits AppSettings with USD`() = runTest {
        // GIVEN
        dataStore.emitSettings(AppSettings(currency = Currency.EUR))

        // WHEN
        sut(Currency.USD)

        // THEN
        val result = dataStore.observe().first()
        assertEquals(Currency.USD, result.currency)
    }

    @Test
    fun `GIVEN initial USD WHEN invoke EUR THEN observe emits AppSettings with EUR`() = runTest {
        // GIVEN
        dataStore.emitSettings(AppSettings(currency = Currency.USD))

        // WHEN
        sut(Currency.EUR)

        // THEN
        val result = dataStore.observe().first()
        assertEquals(Currency.EUR, result.currency)
    }

    // ─── Multiple invocations ─────────────────────────────────────────────────

    @Test
    fun `WHEN invoke called twice THEN dataStore receives both calls in order`() = runTest {
        // WHEN
        sut(Currency.USD)
        sut(Currency.EUR)

        // THEN
        assertEquals(2, dataStore.setCurrencyInvocations.size)
        assertEquals(Currency.USD, dataStore.setCurrencyInvocations[0])
        assertEquals(Currency.EUR, dataStore.setCurrencyInvocations[1])
    }
}
