package com.akole.dividox.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.settings.data.biometric.BiometricAuthenticator
import com.akole.dividox.common.settings.data.biometric.BiometricResult
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.common.settings.domain.usecase.SetCurrencyUseCase
import com.akole.dividox.common.settings.domain.usecase.SetDefaultMarketUseCase
import com.akole.dividox.common.settings.domain.usecase.UpdateBiometricLockUseCase
import com.akole.dividox.component.auth.domain.usecase.SignOutUseCase
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
class SettingsViewModel(
    private val observeAppSettings: ObserveAppSettingsUseCase,
    private val setCurrency: SetCurrencyUseCase,
    private val setDefaultMarket: SetDefaultMarketUseCase,
    private val updateBiometricLock: UpdateBiometricLockUseCase,
    private val signOut: SignOutUseCase,
    private val authenticator: BiometricAuthenticator,
    private val appVersion: String,
) : ViewModel(),
    MVI<SettingsViewState, SettingsViewEvent, SettingsViewSideEffect> by mvi(SettingsViewState()) {

    init {
        updateViewState { copy(appVersion = appVersion) }
        observeSettings()
        checkBiometricAvailability()
    }

    override fun onViewEvent(viewEvent: SettingsViewEvent) {
        when (viewEvent) {
            is SettingsViewEvent.BiometricToggled -> toggleBiometric(viewEvent.enabled)
            is SettingsViewEvent.CurrencyChanged -> changeCurrency(viewEvent.currency)
            is SettingsViewEvent.MarketChanged -> changeMarket(viewEvent.market)
            SettingsViewEvent.FavoritesClicked -> viewModelScope.emitSideEffect(
                SettingsViewSideEffect.Navigation.NavigateToFavorites
            )
            SettingsViewEvent.ExportClicked -> exportPortfolio()
            SettingsViewEvent.NotificationsClicked -> viewModelScope.emitSideEffect(
                SettingsViewSideEffect.OpenUrl("https://help.dividox.app/notifications")
            )
            SettingsViewEvent.HelpClicked -> viewModelScope.emitSideEffect(
                SettingsViewSideEffect.OpenUrl("https://help.dividox.app")
            )
            SettingsViewEvent.AboutClicked -> viewModelScope.emitSideEffect(
                SettingsViewSideEffect.OpenUrl("https://dividox.app/about")
            )
            SettingsViewEvent.TermsClicked -> viewModelScope.emitSideEffect(
                SettingsViewSideEffect.OpenUrl("https://dividox.app/terms")
            )
            SettingsViewEvent.PrivacyClicked -> viewModelScope.emitSideEffect(
                SettingsViewSideEffect.OpenUrl("https://dividox.app/privacy")
            )
            SettingsViewEvent.DeleteAccountClicked -> viewModelScope.emitSideEffect(
                SettingsViewSideEffect.ShowDeleteConfirmDialog
            )
            SettingsViewEvent.DeleteAccountConfirmed -> deleteAccount()
            SettingsViewEvent.SignOutClicked -> viewModelScope.emitSideEffect(
                SettingsViewSideEffect.ShowSignOutConfirmDialog
            )
            SettingsViewEvent.SignOutConfirmed -> signOutUser()
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            observeAppSettings().collect { settings ->
                updateViewState {
                    copy(
                        settings = settings,
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun checkBiometricAvailability() {
        updateViewState { copy(isBiometricAvailable = authenticator.canAuthenticate()) }
    }

    private fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            val result = updateBiometricLock(enabled)
            if (result is BiometricResult.Failure) {
                viewModelScope.emitSideEffect(
                    SettingsViewSideEffect.ShowError(result.reason)
                )
            }
        }
    }

    private fun changeCurrency(currency: Currency) {
        viewModelScope.launch {
            setCurrency(currency)
        }
    }

    private fun changeMarket(market: String) {
        viewModelScope.launch {
            setDefaultMarket(market)
        }
    }

    private fun exportPortfolio() {
        // TODO: implement CSV export to temp dir + share sheet
        viewModelScope.emitSideEffect(
            SettingsViewSideEffect.ShowError("Export feature coming soon")
        )
    }

    private fun deleteAccount() {
        // TODO: delete Firestore user data + sign out
        viewModelScope.launch {
            signOut()
            viewModelScope.emitSideEffect(
                SettingsViewSideEffect.Navigation.NavigateToLogin
            )
        }
    }

    private fun signOutUser() {
        viewModelScope.launch {
            signOut()
            viewModelScope.emitSideEffect(
                SettingsViewSideEffect.Navigation.NavigateToLogin
            )
        }
    }
}
