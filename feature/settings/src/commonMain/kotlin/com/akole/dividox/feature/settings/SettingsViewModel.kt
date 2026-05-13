package com.akole.dividox.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.common.settings.domain.usecase.SetCurrencyUseCase
import com.akole.dividox.common.settings.domain.usecase.SetDefaultMarketUseCase
import com.akole.dividox.component.auth.domain.exception.RecentLoginRequiredException
import com.akole.dividox.component.auth.domain.usecase.SignOutUseCase
import com.akole.dividox.component.portfolio.domain.usecase.ExportPortfolioUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
class SettingsViewModel(
    private val observeAppSettings: ObserveAppSettingsUseCase,
    private val setCurrency: SetCurrencyUseCase,
    private val setDefaultMarket: SetDefaultMarketUseCase,
    private val signOut: SignOutUseCase,
    private val getPortfolio: GetPortfolioUseCase,
    private val exportPortfolio: ExportPortfolioUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val appVersion: String,
) : ViewModel(),
    MVI<SettingsViewState, SettingsViewEvent, SettingsViewSideEffect> by mvi(SettingsViewState()) {

    init {
        updateViewState { copy(appVersion = this@SettingsViewModel.appVersion) }
        observeSettings()
    }

    override fun onViewEvent(viewEvent: SettingsViewEvent) {
        when (viewEvent) {
            is SettingsViewEvent.CurrencyChanged -> changeCurrency(viewEvent.currency)
            is SettingsViewEvent.MarketChanged -> changeMarket(viewEvent.market)
            SettingsViewEvent.FavoritesClicked -> viewModelScope.emitSideEffect(
                SettingsViewSideEffect.Navigation.NavigateToFavorites
            )
            SettingsViewEvent.ExportClicked -> doExportPortfolio()
            SettingsViewEvent.AboutClicked -> viewModelScope.emitSideEffect(
                SettingsViewSideEffect.Navigation.NavigateToAbout
            )
            SettingsViewEvent.TermsClicked -> viewModelScope.emitSideEffect(
                SettingsViewSideEffect.Navigation.NavigateToTerms
            )
            SettingsViewEvent.PrivacyClicked -> viewModelScope.emitSideEffect(
                SettingsViewSideEffect.Navigation.NavigateToPrivacy
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

    private fun doExportPortfolio() {
        viewModelScope.launch {
            updateViewState { copy(isExporting = true) }
            val holdings = getPortfolio.execute().first().getOrElse {
                updateViewState { copy(isExporting = false) }
                viewModelScope.emitSideEffect(SettingsViewSideEffect.ShowError("Failed to load portfolio"))
                return@launch
            }
            val csv = exportPortfolio(holdings)
            updateViewState { copy(isExporting = false) }
            viewModelScope.emitSideEffect(SettingsViewSideEffect.LaunchShareSheet(csvContent = csv))
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            updateViewState { copy(isLoading = true) }
            deleteAccountUseCase().onFailure { error ->
                updateViewState { copy(isLoading = false) }
                val message = if (error is RecentLoginRequiredException) {
                    error.message ?: "Please sign in again to delete your account"
                } else {
                    "Failed to delete account. Please try again."
                }
                viewModelScope.emitSideEffect(SettingsViewSideEffect.ShowError(message))
                return@launch
            }
            viewModelScope.emitSideEffect(SettingsViewSideEffect.Navigation.NavigateToLogin)
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
