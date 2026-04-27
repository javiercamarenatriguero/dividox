package com.akole.dividox.common.ui.resources.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    val visualTransformation = if (isPassword && !isPasswordVisible) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    val effectiveKeyboardType = if (isPassword && !isPasswordVisible) {
        KeyboardType.Password
    } else {
        keyboardType
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
            )
        },
        trailingIcon = if (isPassword) {
            {
                val icon = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                    )
                }
            }
        } else {
            null
        },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = effectiveKeyboardType,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onNext = { onImeAction() },
            onSearch = { onImeAction() },
            onSend = { onImeAction() },
            onGo = { onImeAction() },
        ),
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth(),
    )
}
