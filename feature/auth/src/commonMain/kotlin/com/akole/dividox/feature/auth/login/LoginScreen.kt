package com.akole.dividox.feature.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.mvi.CollectSideEffect
import dividox.common.ui_resources.generated.resources.Res as UiRes
import dividox.common.ui_resources.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import com.akole.dividox.common.ui.resources.components.AppTextField
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.PrimaryButton
import com.akole.dividox.common.ui.resources.components.SectionDivider
import com.akole.dividox.common.ui.resources.components.SocialSignInButton
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.feature.auth.login.LoginContract.LoginSideEffect
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewEvent
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    state: LoginViewState,
    onEvent: (LoginViewEvent) -> Unit,
    sideEffects: Flow<LoginSideEffect>,
    onNavigation: (LoginSideEffect.Navigation) -> Unit,
) {
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordFieldValue by remember { mutableStateOf("") }

    CollectSideEffect(sideEffects) { effect ->
        when (effect) {
            is LoginSideEffect.Navigation -> onNavigation(effect)
            is LoginSideEffect.ShowForgotPasswordDialog -> {
                forgotPasswordFieldValue = effect.email
                showForgotPasswordDialog = true
            }
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            email = forgotPasswordFieldValue,
            onEmailChanged = { forgotPasswordFieldValue = it },
            onConfirm = { showForgotPasswordDialog = false },
            onDismiss = { showForgotPasswordDialog = false },
        )
    }

    Scaffold(
        topBar = {
            DividoxTopAppBar(title = stringResource(UiRes.string.auth_sign_in))
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(UiRes.drawable.ic_dividox),
                contentDescription = null,
                modifier = Modifier.width(200.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(UiRes.string.auth_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(40.dp))

            AppTextField(
                value = state.email,
                onValueChange = { onEvent(LoginViewEvent.OnEmailChanged(it)) },
                placeholder = stringResource(UiRes.string.auth_email),
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.password,
                onValueChange = { onEvent(LoginViewEvent.OnPasswordChanged(it)) },
                placeholder = stringResource(UiRes.string.auth_password),
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                onImeAction = { onEvent(LoginViewEvent.OnSignInClicked) },
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { onEvent(LoginViewEvent.OnForgotPasswordClicked) }) {
                    Text(
                        text = stringResource(UiRes.string.auth_forgot_password),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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

            PrimaryButton(
                text = stringResource(UiRes.string.auth_sign_in),
                onClick = { onEvent(LoginViewEvent.OnSignInClicked) },
                isLoading = state.isLoading,
                enabled = state.email.isNotBlank() && state.password.isNotBlank(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionDivider(text = stringResource(UiRes.string.auth_divider_or_connect_with))

            Spacer(modifier = Modifier.height(24.dp))

            SocialSignInButton(
                onClick = { onEvent(LoginViewEvent.OnGoogleSignInClicked) },
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(UiRes.string.auth_dont_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { onEvent(LoginViewEvent.OnSignUpClicked) }) {
                    Text(
                        text = stringResource(UiRes.string.auth_sign_up),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ForgotPasswordDialog(
    email: String,
    onEmailChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(UiRes.string.auth_reset_password)) },
        text = {
            Column {
                Text(
                    text = stringResource(UiRes.string.auth_forgot_password_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                AppTextField(
                    value = email,
                    onValueChange = onEmailChanged,
                    placeholder = stringResource(UiRes.string.auth_email),
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(UiRes.string.auth_send_reset_link))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(UiRes.string.auth_cancel))
            }
        },
    )
}

@Preview
@Composable
private fun LoginScreenPreview() {
    DividoxTheme {
        LoginScreen(
            state = LoginContract.LoginViewState(),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenFilledPreview() {
    DividoxTheme {
        LoginScreen(
            state = LoginContract.LoginViewState(
                email = "user@dividox.com",
                password = "password123",
            ),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenLoadingPreview() {
    DividoxTheme {
        LoginScreen(
            state = LoginContract.LoginViewState(
                email = "user@dividox.com",
                password = "password123",
                isLoading = true,
            ),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenErrorPreview() {
    DividoxTheme {
        LoginScreen(
            state = LoginContract.LoginViewState(
                email = "user@dividox.com",
                password = "wrong",
                error = "Invalid email or password. Please try again.",
            ),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenDarkPreview() {
    DividoxTheme(darkTheme = true) {
        LoginScreen(
            state = LoginContract.LoginViewState(
                email = "user@dividox.com",
                password = "password123",
            ),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}
