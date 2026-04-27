package com.akole.dividox.feature.auth.login

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.ui.resources.components.AuthDivider
import com.akole.dividox.common.ui.resources.components.AuthTextField
import com.akole.dividox.common.ui.resources.components.PrimaryButton
import com.akole.dividox.common.ui.resources.components.SocialSignInButton
import com.akole.dividox.feature.auth.login.LoginContract.LoginSideEffect
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewEvent
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewState
import kotlinx.coroutines.flow.Flow

@Composable
fun LoginScreen(
    state: LoginViewState,
    onEvent: (LoginViewEvent) -> Unit,
    sideEffects: Flow<LoginSideEffect>,
    onNavigation: (LoginSideEffect.Navigation) -> Unit,
) {
    var forgotPasswordDialogEmail by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordFieldValue by remember { mutableStateOf("") }

    CollectSideEffect(sideEffects) { effect ->
        when (effect) {
            is LoginSideEffect.Navigation -> onNavigation(effect)
            is LoginSideEffect.ShowForgotPasswordDialog -> {
                forgotPasswordDialogEmail = effect.email
                forgotPasswordFieldValue = effect.email
                showForgotPasswordDialog = true
            }
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            email = forgotPasswordFieldValue,
            onEmailChanged = { forgotPasswordFieldValue = it },
            onConfirm = {
                showForgotPasswordDialog = false
                // Emit a fire-and-forget — no dedicated use case call needed here
                // as the ViewModel already handles it via OnForgotPasswordClicked
            },
            onDismiss = { showForgotPasswordDialog = false },
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Brand header
            Text(
                text = "DiviDox",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Track your dividends. Grow your wealth.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email field
            AuthTextField(
                value = state.email,
                onValueChange = { onEvent(LoginViewEvent.OnEmailChanged(it)) },
                placeholder = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password field
            AuthTextField(
                value = state.password,
                onValueChange = { onEvent(LoginViewEvent.OnPasswordChanged(it)) },
                placeholder = "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                onImeAction = { onEvent(LoginViewEvent.OnSignInClicked) },
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Forgot password link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { onEvent(LoginViewEvent.OnForgotPasswordClicked) }) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Error message
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

            // Sign In button
            PrimaryButton(
                text = "Sign In",
                onClick = { onEvent(LoginViewEvent.OnSignInClicked) },
                isLoading = state.isLoading,
                enabled = state.email.isNotBlank() && state.password.isNotBlank(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Divider
            AuthDivider(text = "or connect with")

            Spacer(modifier = Modifier.height(24.dp))

            // Google Sign-In button
            SocialSignInButton(
                text = "Continue with Google",
                onClick = { onEvent(LoginViewEvent.OnGoogleSignInClicked) },
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up link
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { onEvent(LoginViewEvent.OnSignUpClicked) }) {
                    Text(
                        text = "Sign Up",
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
        title = { Text("Reset Password") },
        text = {
            Column {
                Text(
                    text = "Enter your email address to receive a password reset link.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChanged,
                    placeholder = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Send Reset Link")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
