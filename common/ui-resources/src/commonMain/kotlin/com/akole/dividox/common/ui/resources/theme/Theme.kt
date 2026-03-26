package com.akole.dividox.common.ui.resources.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun DividoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DividoxDarkColorScheme else DividoxLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DividoxTypography,
        shapes = DividoxShapes,
        content = content,
    )
}
