package com.akole.dividox.feature.settings

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.common.settings.domain.usecase.SetCurrencyUseCase
import com.akole.dividox.common.settings.domain.usecase.SetDefaultMarketUseCase
import com.akole.dividox.component.auth.domain.exception.RecentLoginRequiredException
import com.akole.dividox.component.auth.domain.usecase.SignOutUseCase
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.usecase.ExportPortfolioUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SettingsViewModelTest {

    private val mockObserveSettings = mockk<ObserveAppSettingsUseCase>()
    private val mockSetCurrency = mockk<SetCurrencyUseCase>()
    private val mockSetDefaultMarket = mockk<SetDefaultMarketUseCase>()
    private val mockSignOut = mockk<SignOutUseCase>()
    private val mockGetPortfolio = mockk<GetPortfolioUseCase>()
    private val mockExportPortfolio = mockk<ExportPortfolioUseCase>()
    private val mockDeleteAccount = mockk<DeleteAccountUseCase>()

    private fun buildViewModel(): SettingsViewModel {
        every { mockObserveSettings() } returns flowOf(AppSettings())
        return SettingsViewModel(
            observeAppSettings = mockObserveSettings,
            setCurrency = mockSetCurrency,
            setDefaultMarket = mockSetDefaultMarket,
            signOut = mockSignOut,
            getPortfolio = mockGetPortfolio,
            exportPortfolio = mockExportPortfolio,
            deleteAccountUseCase = mockDeleteAccount,
            appVersion = "1.0.0",
        )
    }

    @Test
    fun exportClicked_withHoldings_emitsLaunchShareSheet() = runTest {
        // GIVEN
        val holdings = listOf(
            Holding(
                id = HoldingId("1"),
                tickerId = "AAPL",
                shares = 10.0,
                purchasePrice = 150.0,
                purchaseCurrency = Currency.USD,
                purchaseDate = 1_705_276_800_000L,
            ),
        )
        every { mockGetPortfolio.execute() } returns flowOf(Result.success(holdings))
        every { mockExportPortfolio(holdings) } returns "Ticker,Shares,Purchase Price,Currency,Purchase Date\nAAPL,10.0,150.0,USD,2024-01-15"
        val vm = buildViewModel()

        // WHEN
        vm.onViewEvent(SettingsViewEvent.ExportClicked)

        // THEN
        val effect = vm.sideEffect.first()
        assertIs<SettingsViewSideEffect.LaunchShareSheet>(effect)
        assertEquals(true, effect.csvContent.contains("AAPL"))
    }

    @Test
    fun exportClicked_withEmptyPortfolio_emitsLaunchShareSheetWithHeaderOnly() = runTest {
        // GIVEN
        every { mockGetPortfolio.execute() } returns flowOf(Result.success(emptyList()))
        every { mockExportPortfolio(emptyList()) } returns "Ticker,Shares,Purchase Price,Currency,Purchase Date"
        val vm = buildViewModel()

        // WHEN
        vm.onViewEvent(SettingsViewEvent.ExportClicked)

        // THEN
        val effect = vm.sideEffect.first()
        assertIs<SettingsViewSideEffect.LaunchShareSheet>(effect)
        assertEquals("Ticker,Shares,Purchase Price,Currency,Purchase Date", effect.csvContent)
    }

    @Test
    fun exportClicked_whenRepoFails_emitsShowError() = runTest {
        // GIVEN
        every { mockGetPortfolio.execute() } returns flowOf(Result.failure(Exception("Network error")))
        val vm = buildViewModel()

        // WHEN
        vm.onViewEvent(SettingsViewEvent.ExportClicked)

        // THEN
        val effect = vm.sideEffect.first()
        assertIs<SettingsViewSideEffect.ShowError>(effect)
    }

    @Test
    fun deleteAccountConfirmed_onSuccess_emitsNavigateToLogin() = runTest {
        // GIVEN
        coEvery { mockDeleteAccount() } returns Result.success(Unit)
        val vm = buildViewModel()

        // WHEN
        vm.onViewEvent(SettingsViewEvent.DeleteAccountConfirmed)

        // THEN
        val effect = vm.sideEffect.first()
        assertIs<SettingsViewSideEffect.Navigation.NavigateToLogin>(effect)
    }

    @Test
    fun deleteAccountConfirmed_onGenericFailure_emitsShowError() = runTest {
        // GIVEN
        coEvery { mockDeleteAccount() } returns Result.failure(Exception("Server error"))
        val vm = buildViewModel()

        // WHEN
        vm.onViewEvent(SettingsViewEvent.DeleteAccountConfirmed)

        // THEN
        val effect = vm.sideEffect.first()
        assertIs<SettingsViewSideEffect.ShowError>(effect)
        assertEquals("Failed to delete account. Please try again.", effect.message)
    }

    @Test
    fun deleteAccountConfirmed_onRecentLoginRequired_emitsReauthMessage() = runTest {
        // GIVEN
        coEvery { mockDeleteAccount() } returns Result.failure(RecentLoginRequiredException())
        val vm = buildViewModel()

        // WHEN
        vm.onViewEvent(SettingsViewEvent.DeleteAccountConfirmed)

        // THEN
        val effect = vm.sideEffect.first()
        assertIs<SettingsViewSideEffect.ShowError>(effect)
        assertEquals("Please sign in again to delete your account", effect.message)
    }
}
