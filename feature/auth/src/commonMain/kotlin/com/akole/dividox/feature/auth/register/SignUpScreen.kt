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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.ui.resources.components.AuthTextField
import com.akole.dividox.common.ui.resources.components.PrimaryButton
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpSideEffect
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewEvent
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewState
import kotlinx.coroutines.flow.Flow

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
            // Heading
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Join DiviDox and start tracking your dividends.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Full Name field
            AuthTextField(
                value = state.name,
                onValueChange = { onEvent(SignUpViewEvent.OnNameChanged(it)) },
                placeholder = "Full Name",
                leadingIcon = Icons.Default.Person,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email field
            AuthTextField(
                value = state.email,
                onValueChange = { onEvent(SignUpViewEvent.OnEmailChanged(it)) },
                placeholder = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password field
            AuthTextField(
                value = state.password,
                onValueChange = { onEvent(SignUpViewEvent.OnPasswordChanged(it)) },
                placeholder = "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                onImeAction = { if (isFormValid) onEvent(SignUpViewEvent.OnCreateAccountClicked) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Terms checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = state.termsAccepted,
                    onCheckedChange = { onEvent(SignUpViewEvent.OnTermsChanged(it)) },
                )
                Text(
                    text = "I agree to the Terms of Service and Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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

            // Create Account button
            PrimaryButton(
                text = "Create Account",
                onClick = { onEvent(SignUpViewEvent.OnCreateAccountClicked) },
                isLoading = state.isLoading,
                enabled = isFormValid,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { onEvent(SignUpViewEvent.OnSignInClicked) }) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
