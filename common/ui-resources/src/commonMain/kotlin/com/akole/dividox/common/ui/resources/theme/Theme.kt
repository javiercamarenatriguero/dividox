package com.akole.dividox.common.ui.resources.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

val MaterialTheme.spacing: DividoxSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalDividoxSpacing.current

val MaterialTheme.extendedColors: DividoxExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalDividoxExtendedColors.current

@Composable
fun DividoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DividoxDarkColorScheme else DividoxLightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
    val typography = buildDividoxTypography(rememberInterFontFamily())
    CompositionLocalProvider(
        LocalDividoxSpacing provides DividoxSpacing(),
        LocalDividoxExtendedColors provides extendedColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = DividoxShapes,
            content = content,
        )
    }
}
