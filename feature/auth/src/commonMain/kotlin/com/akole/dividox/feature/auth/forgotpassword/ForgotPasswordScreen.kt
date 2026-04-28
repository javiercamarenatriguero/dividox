package com.akole.dividox.feature.auth.forgotpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.ui.resources.components.AppTextField
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.PrimaryButton
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordSideEffect
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordViewEvent
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordViewState
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordViewState,
    onEvent: (ForgotPasswordViewEvent) -> Unit,
    sideEffects: Flow<ForgotPasswordSideEffect>,
    onNavigation: (ForgotPasswordSideEffect.Navigation) -> Unit,
) {
    CollectSideEffect(sideEffects) { effect ->
        when (effect) {
            is ForgotPasswordSideEffect.Navigation -> onNavigation(effect)
        }
    }

    Scaffold(
        topBar = {
            DividoxTopAppBar(
                title = stringResource(Res.string.auth_reset_password),
                onBack = { onEvent(ForgotPasswordViewEvent.OnBackClicked) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.auth_forgot_password_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppTextField(
                value = state.email,
                onValueChange = { onEvent(ForgotPasswordViewEvent.OnEmailChanged(it)) },
                placeholder = stringResource(Res.string.auth_email),
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                onImeAction = {
                    if (state.email.isNotBlank()) onEvent(ForgotPasswordViewEvent.OnSendResetLinkClicked)
                },
                enabled = !state.isSuccess,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
            }

            if (state.isSuccess) {
                Text(
                    text = stringResource(Res.string.auth_forgot_password_confirmation),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                PrimaryButton(
                    text = stringResource(Res.string.auth_send_reset_link),
                    onClick = { onEvent(ForgotPasswordViewEvent.OnSendResetLinkClicked) },
                    isLoading = state.isLoading,
                    enabled = state.email.isNotBlank(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ForgotPasswordScreenPreview() {
    DividoxTheme {
        ForgotPasswordScreen(
            state = ForgotPasswordViewState(),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}

@Preview
@Composable
private fun ForgotPasswordScreenSuccessPreview() {
    DividoxTheme {
        ForgotPasswordScreen(
            state = ForgotPasswordViewState(
                email = "user@dividox.com",
                isSuccess = true,
            ),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}
