package com.akole.dividox.feature.settings

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState
import com.akole.dividox.common.settings.domain.model.AppSettings

data class SettingsViewState(
    val settings: AppSettings? = null,
    val isBiometricAvailable: Boolean = false,
    val appVersion: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
) : ViewState

sealed interface SettingsViewEvent : ViewEvent {
    data class BiometricToggled(val enabled: Boolean) : SettingsViewEvent
    data class CurrencyChanged(val currency: Currency) : SettingsViewEvent
    data object FavoritesClicked : SettingsViewEvent
    data object ExportClicked : SettingsViewEvent
    data object NotificationsClicked : SettingsViewEvent
    data object HelpClicked : SettingsViewEvent
    data object AboutClicked : SettingsViewEvent
    data object TermsClicked : SettingsViewEvent
    data object PrivacyClicked : SettingsViewEvent
    data object DeleteAccountClicked : SettingsViewEvent
    data object DeleteAccountConfirmed : SettingsViewEvent
    data object SignOutClicked : SettingsViewEvent
    data object SignOutConfirmed : SettingsViewEvent
}

sealed interface SettingsViewSideEffect : SideEffect {
    sealed interface Navigation : SettingsViewSideEffect {
        data object NavigateToFavorites : Navigation
        data object NavigateToLogin : Navigation
    }
    data object ShowDeleteConfirmDialog : SettingsViewSideEffect
    data object ShowSignOutConfirmDialog : SettingsViewSideEffect
    data class LaunchShareSheet(val fileUri: String) : SettingsViewSideEffect
    data class OpenUrl(val url: String) : SettingsViewSideEffect
    data class ShowError(val message: String) : SettingsViewSideEffect
}
