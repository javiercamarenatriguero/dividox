package com.akole.dividox.common.ui.resources.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.akole.dividox.common.ui.resources.theme.spacing

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = CircleShape,
        modifier = modifier.fillMaxWidth().heightIn(min = MaterialTheme.spacing.buttonMinHeight),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = MaterialTheme.spacing.xSmall / 2,
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
