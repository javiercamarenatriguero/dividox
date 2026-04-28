package com.akole.dividox.feature.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.ui.resources.components.AppTextField
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.components.PrimaryButton
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpSideEffect
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewEvent
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewState
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource

@Suppress("LongMethod", "FunctionNaming")
@Composable
fun SignUpScreen(
    state: SignUpViewState,
    onEvent: (SignUpViewEvent) -> Unit,
    sideEffects: Flow<SignUpSideEffect>,
    onNavigation: (SignUpSideEffect.Navigation) -> Unit,
) {
    CollectSideEffect(sideEffects) { effect ->
        when (effect) {
            is SignUpSideEffect.Navigation -> onNavigation(effect)
        }
    }

    val isFormValid = state.name.isNotBlank() &&
        state.email.isNotBlank() &&
        state.password.isNotBlank() &&
        state.termsAccepted

    Scaffold(
        topBar = {
            DividoxTopAppBar(
                title = stringResource(Res.string.auth_create_account),
                onBack = { onEvent(SignUpViewEvent.OnSignInClicked) },
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
                text = stringResource(Res.string.auth_join_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            AppTextField(
                value = state.name,
                onValueChange = { onEvent(SignUpViewEvent.OnNameChanged(it)) },
                placeholder = stringResource(Res.string.auth_full_name),
                leadingIcon = Icons.Default.Person,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.email,
                onValueChange = { onEvent(SignUpViewEvent.OnEmailChanged(it)) },
                placeholder = stringResource(Res.string.auth_email),
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.password,
                onValueChange = { onEvent(SignUpViewEvent.OnPasswordChanged(it)) },
                placeholder = stringResource(Res.string.auth_password),
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                onImeAction = { if (isFormValid) onEvent(SignUpViewEvent.OnCreateAccountClicked) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = state.termsAccepted,
                    onCheckedChange = { onEvent(SignUpViewEvent.OnTermsChanged(it)) },
                )
                Text(
                    text = stringResource(Res.string.auth_terms_agreement),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                text = stringResource(Res.string.auth_create_account),
                onClick = { onEvent(SignUpViewEvent.OnCreateAccountClicked) },
                isLoading = state.isLoading,
                enabled = isFormValid,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(Res.string.auth_already_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { onEvent(SignUpViewEvent.OnSignInClicked) }) {
                    Text(
                        text = stringResource(Res.string.auth_sign_in),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SignUpScreenPreview() {
    DividoxTheme {
        SignUpScreen(
            state = SignUpContract.SignUpViewState(),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}

@Preview
@Composable
private fun SignUpScreenFilledPreview() {
    DividoxTheme {
        SignUpScreen(
            state = SignUpContract.SignUpViewState(
                name = "Javier Camarena",
                email = "javier@dividox.com",
                password = "securepass",
                termsAccepted = true,
            ),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}

@Preview
@Composable
private fun SignUpScreenLoadingPreview() {
    DividoxTheme {
        SignUpScreen(
            state = SignUpContract.SignUpViewState(
                name = "Javier Camarena",
                email = "javier@dividox.com",
                password = "securepass",
                termsAccepted = true,
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
private fun SignUpScreenErrorPreview() {
    DividoxTheme {
        SignUpScreen(
            state = SignUpContract.SignUpViewState(
                name = "Javier Camarena",
                email = "javier@dividox.com",
                password = "pass",
                termsAccepted = true,
                error = "An account with this email already exists.",
            ),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}

@Preview
@Composable
private fun SignUpScreenDarkPreview() {
    DividoxTheme(darkTheme = true) {
        SignUpScreen(
            state = SignUpContract.SignUpViewState(
                name = "Javier Camarena",
                email = "javier@dividox.com",
                password = "securepass",
                termsAccepted = true,
            ),
            onEvent = {},
            sideEffects = emptyFlow(),
            onNavigation = {},
        )
    }
}
