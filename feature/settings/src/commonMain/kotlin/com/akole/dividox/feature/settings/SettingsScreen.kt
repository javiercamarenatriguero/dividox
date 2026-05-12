package com.akole.dividox.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.ExchangeMarket
import com.akole.dividox.common.ui.resources.format.flag
import com.akole.dividox.common.ui.resources.format.nameRes
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.section_settings
import dividox.common.ui_resources.generated.resources.settings_about
import dividox.common.ui_resources.generated.resources.settings_close
import dividox.common.ui_resources.generated.resources.settings_currency
import dividox.common.ui_resources.generated.resources.settings_currency_picker_title
import dividox.common.ui_resources.generated.resources.settings_default_market
import dividox.common.ui_resources.generated.resources.settings_market_picker_title
import dividox.common.ui_resources.generated.resources.settings_delete_account
import dividox.common.ui_resources.generated.resources.settings_delete_confirm_message
import dividox.common.ui_resources.generated.resources.settings_delete_confirm_title
import dividox.common.ui_resources.generated.resources.settings_export
import dividox.common.ui_resources.generated.resources.settings_favorites
import dividox.common.ui_resources.generated.resources.settings_privacy
import dividox.common.ui_resources.generated.resources.settings_section_data
import dividox.common.ui_resources.generated.resources.settings_section_legal
import dividox.common.ui_resources.generated.resources.settings_section_portfolio
import dividox.common.ui_resources.generated.resources.settings_section_preferences
import dividox.common.ui_resources.generated.resources.settings_sign_out
import dividox.common.ui_resources.generated.resources.settings_sign_out_confirm_message
import dividox.common.ui_resources.generated.resources.settings_sign_out_confirm_title
import dividox.common.ui_resources.generated.resources.settings_terms
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    state: SettingsViewState,
    onEvent: (SettingsViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSignOutDialog by retain { mutableStateOf(false) }
    var showDeleteDialog by retain { mutableStateOf(false) }
    var showCurrencyPicker by retain { mutableStateOf(false) }
    var showMarketPicker by retain { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            DividoxTopAppBar(title = stringResource(Res.string.section_settings))
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.spacing.medium),
        ) {
            if (state.settings != null) {
                Spacer(Modifier.height(MaterialTheme.spacing.medium))

                // Preferences
                SectionLabel(stringResource(Res.string.settings_section_preferences))
                SettingsCard {
                    SettingsRow(
                        label = stringResource(Res.string.settings_currency),
                        icon = Icons.Filled.AttachMoney,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = { showCurrencyPicker = true },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
                            ) {
                                Text(
                                    "${state.settings.currency.symbol} ${state.settings.currency.code}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Icon(
                                    Icons.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
                                )
                            }
                        },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                    val currentMarket = ExchangeMarket.entries.firstOrNull {
                        it.name == state.settings.defaultMarket
                    } ?: ExchangeMarket.ALL
                    SettingsRow(
                        label = stringResource(Res.string.settings_default_market),
                        icon = Icons.Filled.Language,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = { showMarketPicker = true },
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
                            ) {
                                Text(
                                    "${currentMarket.emoji} ${currentMarket.label}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Icon(
                                    Icons.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
                                )
                            }
                        },
                    )
                }

                Spacer(Modifier.height(MaterialTheme.spacing.medium))

                // Portfolio
                SectionLabel(stringResource(Res.string.settings_section_portfolio))
                SettingsCard {
                    SettingsRow(
                        label = stringResource(Res.string.settings_favorites),
                        icon = Icons.Filled.Star,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = { onEvent(SettingsViewEvent.FavoritesClicked) },
                    )
                }

                Spacer(Modifier.height(MaterialTheme.spacing.medium))

                // Legal & About
                SectionLabel(stringResource(Res.string.settings_section_legal))
                SettingsCard {
                    SettingsRow(
                        label = stringResource(Res.string.settings_about),
                        icon = Icons.Filled.Info,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = { onEvent(SettingsViewEvent.AboutClicked) },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                    SettingsRow(
                        label = stringResource(Res.string.settings_terms),
                        icon = Icons.Filled.Gavel,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = { onEvent(SettingsViewEvent.TermsClicked) },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                    SettingsRow(
                        label = stringResource(Res.string.settings_privacy),
                        icon = Icons.Filled.Lock,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = { onEvent(SettingsViewEvent.PrivacyClicked) },
                    )
                }

                Spacer(Modifier.height(MaterialTheme.spacing.medium))

                // Data Management
                SectionLabel(stringResource(Res.string.settings_section_data))
                SettingsCard {
                    SettingsRow(
                        label = stringResource(Res.string.settings_export),
                        icon = Icons.Filled.Share,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = { onEvent(SettingsViewEvent.ExportClicked) },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                    SettingsRow(
                        label = stringResource(Res.string.settings_delete_account),
                        icon = Icons.Filled.DeleteForever,
                        iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                        iconTint = MaterialTheme.colorScheme.onErrorContainer,
                        onClick = { showDeleteDialog = true },
                        isDestructive = true,
                    )
                }

                Spacer(Modifier.height(MaterialTheme.spacing.medium))

                // Sign Out
                SettingsCard {
                    SettingsRow(
                        label = stringResource(Res.string.settings_sign_out),
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                        iconTint = MaterialTheme.colorScheme.onErrorContainer,
                        onClick = { showSignOutDialog = true },
                        isDestructive = true,
                    )
                }

                Spacer(Modifier.height(MaterialTheme.spacing.large))

                Text(
                    "v${state.appVersion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Spacer(Modifier.height(MaterialTheme.spacing.large))
            }
        }
    }

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

    if (showMarketPicker) {
        val currentMarket = ExchangeMarket.entries.firstOrNull {
            it.name == state.settings?.defaultMarket
        } ?: ExchangeMarket.ALL
        MarketPickerDialog(
            current = currentMarket,
            onSelected = { market ->
                onEvent(SettingsViewEvent.MarketChanged(market.name))
                showMarketPicker = false
            },
            onDismiss = { showMarketPicker = false },
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            title = { Text(stringResource(Res.string.settings_sign_out_confirm_title)) },
            text = { Text(stringResource(Res.string.settings_sign_out_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { onEvent(SettingsViewEvent.SignOutConfirmed); showSignOutDialog = false }) {
                    Text(stringResource(Res.string.settings_sign_out), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(Res.string.settings_close))
                }
            },
            onDismissRequest = { showSignOutDialog = false },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            title = { Text(stringResource(Res.string.settings_delete_confirm_title)) },
            text = { Text(stringResource(Res.string.settings_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { onEvent(SettingsViewEvent.DeleteAccountConfirmed); showDeleteDialog = false }) {
                    Text(stringResource(Res.string.settings_delete_account), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(Res.string.settings_close))
                }
            },
            onDismissRequest = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(
            start = MaterialTheme.spacing.small,
            bottom = MaterialTheme.spacing.xSmall,
        ),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MaterialTheme.spacing.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        content()
    }
}

@Composable
private fun SettingsRow(
    label: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(MaterialTheme.spacing.xLarge)
                .clip(RoundedCornerShape(MaterialTheme.spacing.small))
                .background(iconContainerColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
            )
        }
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDestructive) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
        )
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
            )
        }
    }
}

@Composable
private fun CurrencyPickerDialog(
    current: Currency,
    onSelected: (Currency) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(Res.string.settings_currency_picker_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                Currency.entries.forEachIndexed { index, currency ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(currency) }
                            .padding(
                                horizontal = MaterialTheme.spacing.small,
                                vertical = MaterialTheme.spacing.medium,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            currency.flag(),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(currency.nameRes()),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "${currency.code} · ${currency.symbol.trim()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (currency == current) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
                            )
                        } else {
                            Spacer(Modifier.width(MaterialTheme.spacing.iconSmall))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.settings_close))
            }
        },
        onDismissRequest = onDismiss,
    )
}

@Composable
private fun MarketPickerDialog(
    current: ExchangeMarket,
    onSelected: (ExchangeMarket) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(Res.string.settings_market_picker_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                ExchangeMarket.entries.forEachIndexed { index, market ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(market) }
                            .padding(
                                horizontal = MaterialTheme.spacing.small,
                                vertical = MaterialTheme.spacing.medium,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            market.emoji,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            market.label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        if (market == current) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
                            )
                        } else {
                            Spacer(Modifier.width(MaterialTheme.spacing.iconSmall))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.settings_close))
            }
        },
        onDismissRequest = onDismiss,
    )
}
