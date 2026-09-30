package com.akole.dividox.feature.portfolio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import kotlin.time.Clock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.connectivity.ConnectivityBannerHost
import com.akole.dividox.common.ui.resources.components.connectivity.LocalNetworkConnectivityManager
import com.akole.dividox.common.ui.resources.format.formatPrice
import com.akole.dividox.common.ui.resources.format.formatTwoDecimals
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.common.ui.resources.theme.spacing
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.model.displayName
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.action_add_position
import dividox.common.ui_resources.generated.resources.action_cancel
import dividox.common.ui_resources.generated.resources.action_delete
import dividox.common.ui_resources.generated.resources.action_update_position
import dividox.common.ui_resources.generated.resources.cd_clear_field
import dividox.common.ui_resources.generated.resources.cd_delete
import dividox.common.ui_resources.generated.resources.dialog_remove_message
import dividox.common.ui_resources.generated.resources.dialog_remove_title
import dividox.common.ui_resources.generated.resources.holding_security_unavailable
import dividox.common.ui_resources.generated.resources.label_estimated_value
import dividox.common.ui_resources.generated.resources.label_price_per_share
import dividox.common.ui_resources.generated.resources.label_purchase_date
import dividox.common.ui_resources.generated.resources.label_shares
import dividox.common.ui_resources.generated.resources.label_unknown_position
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

private const val FOCUS_AFTER_TRANSITION_MS = 400L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingScreen(
    viewModel: HoldingViewModel,
    onBack: () -> Unit,
    onPositionSaved: () -> Unit,
    onPositionDeleted: () -> Unit,
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    // State-based navigation: triggers reliably on recomposition
    LaunchedEffect(state.operationCompleted) {
        if (state.operationCompleted) {
            if (state.operationIsDelete) onPositionDeleted() else onPositionSaved()
        }
    }

    // Handle other side effects (errors, etc.)
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is HoldingContract.HoldingSideEffect.PositionSaved -> Unit
                is HoldingContract.HoldingSideEffect.PositionDeleted -> Unit
                is HoldingContract.HoldingSideEffect.ShowError -> {
                    // TODO: Show error snackbar
                }
                HoldingContract.HoldingSideEffect.HapticFeedback -> {
                    // TODO: Trigger haptic on Android
                }
            }
        }
    }

    val title = when (state.mode) {
        HoldingContract.Mode.ADD -> stringResource(Res.string.action_add_position)
        HoldingContract.Mode.EDIT -> stringResource(Res.string.action_update_position)
    }

    val connectivityManager = LocalNetworkConnectivityManager.current

    Scaffold(
        topBar = {
            DividoxTopAppBar(
                title = title,
                onBack = onBack,
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ConnectivityBannerHost(connectivityFlow = connectivityManager.observeConnectivity())
            HoldingScreenContent(
                state = state,
                onEvent = viewModel::onEvent,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(bottom = MaterialTheme.spacing.large),
            )
        }
    }

    if (state.showDeleteConfirmation) {
        DeleteConfirmationDialog(
            ticker = state.selectedSecurity?.ticker ?: stringResource(Res.string.label_unknown_position),
            onConfirm = {
                viewModel.onEvent(HoldingContract.HoldingViewEvent.ConfirmDeleteClicked)
            },
            onCancel = {
                viewModel.onEvent(HoldingContract.HoldingViewEvent.CancelDeleteClicked)
            },
        )
    }
}

@Composable
private fun HoldingScreenContent(
    state: HoldingContract.HoldingViewState,
    onEvent: (HoldingContract.HoldingViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        val security = state.selectedSecurity
        when {
            state.isLoadingSecurity -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.xxLarge),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            security == null -> Text(
                text = stringResource(Res.string.holding_security_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = MaterialTheme.spacing.large),
            )

            else -> HoldingForm(state = state, security = security, onEvent = onEvent)
        }
    }
}

@Composable
private fun HoldingForm(
    state: HoldingContract.HoldingViewState,
    security: StockQuote,
    onEvent: (HoldingContract.HoldingViewEvent) -> Unit,
) {
    val sharesFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    // Security is already chosen: jump straight to the only field that has no sensible default.
    // Wait for the slide-in transition, otherwise the focus request is dropped.
    LaunchedEffect(Unit) {
        delay(FOCUS_AFTER_TRANSITION_MS)
        runCatching { sharesFocus.requestFocus() }
        keyboard?.show()
    }
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
        // Selected security display
        SelectedSecurityCard(security = security)

        // Shares input
        OutlinedTextField(
            value = state.shares,
            onValueChange = { input ->
                if (input.all { it.isDigit() }) {
                    onEvent(HoldingContract.HoldingViewEvent.SharesChanged(input))
                }
            },
            label = { Text(stringResource(Res.string.label_shares)) },
            modifier = Modifier.fillMaxWidth().focusRequester(sharesFocus),
            singleLine = true,
            trailingIcon = {
                ClearFieldButton(visible = state.shares.isNotEmpty()) {
                    onEvent(HoldingContract.HoldingViewEvent.SharesChanged(""))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        )

        // Price per share input
        OutlinedTextField(
            value = state.pricePerShare,
            onValueChange = { input ->
                if (input.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onEvent(HoldingContract.HoldingViewEvent.PricePerShareChanged(input))
                }
            },
            label = { Text(stringResource(Res.string.label_price_per_share)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                ClearFieldButton(visible = state.pricePerShare.isNotEmpty()) {
                    onEvent(HoldingContract.HoldingViewEvent.PricePerShareChanged(""))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )

        // Currency selector (chips or dropdown)
        CurrencySelector(
            selectedCurrency = state.currency,
            onCurrencySelected = { currency ->
                onEvent(HoldingContract.HoldingViewEvent.CurrencyChanged(currency))
            },
        )

        // Purchase date picker
        PurchaseDateField(
            dateMillis = state.purchaseDateMillis,
            onDateSelected = { millis ->
                onEvent(HoldingContract.HoldingViewEvent.PurchaseDateChanged(millis))
            },
        )

        // Estimated total display
        EstimatedTotalCard(
            total = state.estimatedTotal,
            currency = state.currency,
        )

        // Error message
        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = MaterialTheme.spacing.small),
            )
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            if (state.mode == HoldingContract.Mode.EDIT) {
                Button(
                    onClick = { onEvent(HoldingContract.HoldingViewEvent.DeleteClicked) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = MaterialTheme.spacing.buttonMinHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    enabled = true,
                ) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.cd_delete))
                    Text(stringResource(Res.string.action_delete))
                }
            }

            Button(
                onClick = { onEvent(HoldingContract.HoldingViewEvent.ConfirmClicked) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = MaterialTheme.spacing.buttonMinHeight),
                enabled = state.shares.isNotBlank() && state.pricePerShare.isNotBlank(),
            ) {
                Text(
                    text = when (state.mode) {
                        HoldingContract.Mode.ADD -> stringResource(Res.string.action_add_position)
                        HoldingContract.Mode.EDIT -> stringResource(Res.string.action_update_position)
                    }
                )
            }
        }
    }
}

@Composable
private fun ClearFieldButton(visible: Boolean, onClear: () -> Unit) {
    if (!visible) return
    IconButton(onClick = onClear) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(Res.string.cd_clear_field),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SelectedSecurityCard(security: StockQuote) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.small),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = security.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = security.price.formatPrice("USD"),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Text(
                text = when {
                    security.change > 0 -> "+${security.change.formatTwoDecimals()}%"
                    else -> "${security.change.formatTwoDecimals()}%"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (security.change >= 0)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CurrencySelector(
    selectedCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit,
) {
    val currencies = listOf(
        Currency.USD,
        Currency.EUR,
        Currency.GBP,
        Currency.GBX,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.small),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        currencies.forEach { currency ->
            val isSelected = currency == selectedCurrency
            if (isSelected) {
                Button(
                    onClick = { onCurrencySelected(currency) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(MaterialTheme.spacing.xSmall),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = currency.code,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { onCurrencySelected(currency) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(MaterialTheme.spacing.xSmall),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = currency.code,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun EstimatedTotalCard(
    total: Double,
    currency: Currency,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.medium),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
        ) {
            Text(
                text = stringResource(Res.string.label_estimated_value),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = total.formatPrice(currency),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteConfirmationDialog(
    ticker: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onCancel,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(MaterialTheme.spacing.large),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                Text(
                    text = stringResource(Res.string.dialog_remove_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(Res.string.dialog_remove_message, ticker),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = MaterialTheme.spacing.buttonMinHeight),
                    ) {
                        Text(stringResource(Res.string.action_cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = MaterialTheme.spacing.buttonMinHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(stringResource(Res.string.action_delete))
                    }
                }
            }
        }
    }
}

// ===== PREVIEWS =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchaseDateField(
    dateMillis: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by retain { mutableStateOf(false) }
    val todayMillis = Clock.System.now().toEpochMilliseconds()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
        },
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = dateMillis.toFormattedDate(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.label_purchase_date)) },
            trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(modifier = Modifier.matchParentSize().clickable { showPicker = true })
    }
}

private fun Long.toFormattedDate(): String {
    val date = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
    val day = date.day.toString().padStart(2, '0')
    val month = (date.month.ordinal + 1).toString().padStart(2, '0')
    return "$day/$month/${date.year}"
}


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoldingScreenAddLoadingPreview() {
    DividoxTheme {
        HoldingScreenContent(
            state = HoldingContract.HoldingViewState(
                mode = HoldingContract.Mode.ADD,
                isLoadingSecurity = true,
            ),
            onEvent = {},
        )
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoldingScreenEditPrefilledPreview() {
    val mockQuote = StockQuote(
        ticker = "MSFT",
        price = 350.0,
        change = -2.5,
        changePercent = -0.71,
        currency = "USD",
        lastUpdated = kotlin.time.Instant.parse("2024-01-20T00:00:00Z"),
    )
    DividoxTheme {
        HoldingScreenContent(
            state = HoldingContract.HoldingViewState(
                mode = HoldingContract.Mode.EDIT,
                holdingId = HoldingId("h1"),
                selectedSecurity = mockQuote,
                shares = "10",
                pricePerShare = "320.0",
                estimatedTotal = 3200.0,
            ),
            onEvent = {},
        )
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoldingScreenEditDeleteDialogPreview() {
    val mockQuote = StockQuote(
        ticker = "TSLA",
        price = 200.0,
        change = 1.0,
        changePercent = 0.5,
        currency = "USD",
        lastUpdated = kotlin.time.Instant.parse("2024-01-20T00:00:00Z"),
    )
    DividoxTheme {
        HoldingScreenContent(
            state = HoldingContract.HoldingViewState(
                mode = HoldingContract.Mode.EDIT,
                holdingId = HoldingId("h2"),
                selectedSecurity = mockQuote,
                shares = "5",
                pricePerShare = "200.0",
                showDeleteConfirmation = true,
            ),
            onEvent = {},
        )
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoldingScreenAddDarkPreview() {
    val mockQuote = StockQuote(
        ticker = "AAPL",
        price = 150.0,
        change = 5.0,
        changePercent = 3.33,
        currency = "USD",
        lastUpdated = kotlin.time.Instant.parse("2024-01-20T00:00:00Z"),
    )
    DividoxTheme(darkTheme = true) {
        HoldingScreenContent(
            state = HoldingContract.HoldingViewState(
                mode = HoldingContract.Mode.ADD,
                selectedSecurity = mockQuote,
                shares = "10",
                pricePerShare = "150",
                estimatedTotal = 1500.0,
            ),
            onEvent = {},
        )
    }
}
