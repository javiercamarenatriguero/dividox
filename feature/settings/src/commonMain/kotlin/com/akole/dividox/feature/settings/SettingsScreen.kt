package com.akole.dividox.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.ui.resources.compose.spacing

@Composable
fun SettingsScreen(
    state: SettingsViewState,
    onEvent: (SettingsViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Header
            Text(
                "Profile & Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(
                        horizontal = MaterialTheme.spacing.large,
                        vertical = MaterialTheme.spacing.medium,
                    ),
            )

            if (state.settings != null) {
                // Security & Preferences
                if (state.isBiometricAvailable) {
                    SectionHeader("Security & Preferences")
                    BiometricRow(
                        enabled = state.settings.biometricLockEnabled,
                        onToggle = { onEvent(SettingsViewEvent.BiometricToggled(it)) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large))
                }

                CurrencyRow(
                    currency = state.settings.currency,
                    onShowPicker = { showCurrencyPicker = true },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large))

                // Portfolio
                SectionHeader("Portfolio")
                SettingsRowButton(
                    label = "Favorites",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { onEvent(SettingsViewEvent.FavoritesClicked) },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large))

                // Support & Help
                SectionHeader("Support & Help")
                SettingsRowButton(
                    label = "Help Center",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { onEvent(SettingsViewEvent.HelpClicked) },
                )
                SettingsRowButton(
                    label = "Contact Support",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { onEvent(SettingsViewEvent.NotificationsClicked) },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large))

                // Legal & About
                SectionHeader("Legal & About")
                SettingsRowButton(
                    label = "About DiviDox",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { onEvent(SettingsViewEvent.AboutClicked) },
                )
                SettingsRowButton(
                    label = "Terms & Conditions",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { onEvent(SettingsViewEvent.TermsClicked) },
                )
                SettingsRowButton(
                    label = "Privacy Policy",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { onEvent(SettingsViewEvent.PrivacyClicked) },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large))

                // Data Management
                SectionHeader("Data Management")
                SettingsRowButton(
                    label = "Export Portfolio",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { onEvent(SettingsViewEvent.ExportClicked) },
                )
                SettingsRowButton(
                    label = "Delete Account",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { showDeleteDialog = true },
                    isDestructive = true,
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large))

                // Sign Out button
                TextButton(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.medium),
                ) {
                    Text(
                        "Sign Out",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Footer
                Text(
                    "v${state.appVersion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(
                            horizontal = MaterialTheme.spacing.large,
                            vertical = MaterialTheme.spacing.large,
                        )
                        .align(Alignment.CenterHorizontally),
                )
            }
        }
    }

    // Dialogs
    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            current = state.settings?.currency ?: Currency.EUR,
            onSelected = { currency ->
                onEvent(SettingsViewEvent.CurrencyChanged(currency))
                showCurrencyPicker = false
            },
            onDismiss = { showCurrencyPicker = false },
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = { onEvent(SettingsViewEvent.SignOutConfirmed); showSignOutDialog = false }) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            },
            onDismissRequest = { showSignOutDialog = false },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            title = { Text("Delete Account") },
            text = { Text("Are you sure? This action is permanent and cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onEvent(SettingsViewEvent.DeleteAccountConfirmed); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            onDismissRequest = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            horizontal = MaterialTheme.spacing.large,
            vertical = MaterialTheme.spacing.medium,
        ),
    )
}

@Composable
private fun BiometricRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Biometric Lock", style = MaterialTheme.typography.bodyLarge)
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun CurrencyRow(currency: Currency, onShowPicker: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onShowPicker)
            .padding(MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Currency", style = MaterialTheme.typography.bodyLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(currency.code, style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun SettingsRowButton(
    label: String,
    icon: androidx.compose.material.icons.materialIcon,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDestructive) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
        )
        Icon(icon, contentDescription = null)
    }
}

@Composable
private fun CurrencyPickerDialog(
    current: Currency,
    onSelected: (Currency) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text("Select Currency") },
        text = {
            Column {
                Currency.entries.forEach { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(currency) }
                            .padding(MaterialTheme.spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = currency == current,
                            onClick = { onSelected(currency) },
                        )
                        Text(currency.code)
                    }
                }
            }
        },
        confirmButton = {},
        onDismissRequest = onDismiss,
    )
}
