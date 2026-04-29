package com.akole.dividox.common.ui.resources.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ─── Light tokens ────────────────────────────────────────────────────────────
// Source: .stitch/DESIGN.md · DiviDox Finance · Stitch asset f6fd49b3589b42809306d7cdea72c58d

private val light_primary = Color(0xFF304169)
private val light_onPrimary = Color(0xFFFFFFFF)
private val light_primaryContainer = Color(0xFF0873DF)
private val light_onPrimaryContainer = Color(0xFFFEFCFF)
private val light_secondary = Color(0xFF465F89)
private val light_onSecondary = Color(0xFFFFFFFF)
private val light_secondaryContainer = Color(0xFFB7CFFF)
private val light_onSecondaryContainer = Color(0xFF405882)
private val light_tertiary = Color(0xFF964400)
private val light_onTertiary = Color(0xFFFFFFFF)
private val light_tertiaryContainer = Color(0xFFBD5700)
private val light_onTertiaryContainer = Color(0xFFFFFBFF)
private val light_error = Color(0xFFBA1A1A)
private val light_onError = Color(0xFFFFFFFF)
private val light_errorContainer = Color(0xFFFFDAD6)
private val light_onErrorContainer = Color(0xFF93000A)
private val light_background = Color(0xFFF9F9FF)
private val light_onBackground = Color(0xFF181C22)
private val light_surface = Color(0xFFF9F9FF)
private val light_onSurface = Color(0xFF181C22)
private val light_surfaceVariant = Color(0xFFE0E2EC)
private val light_onSurfaceVariant = Color(0xFF414753)
private val light_outline = Color(0xFF717785)
private val light_outlineVariant = Color(0xFFC1C6D5)
private val light_scrim = Color(0xFF000000)
private val light_inverseSurface = Color(0xFF2D3038)
private val light_inverseOnSurface = Color(0xFFEFF0FA)
private val light_inversePrimary = Color(0xFFAAC7FF)
private val light_surfaceTint = Color(0xFF005DB8)
private val light_surfaceBright = Color(0xFFF9F9FF)
private val light_surfaceDim = Color(0xFFD8DAE3)
private val light_surfaceContainer = Color(0xFFECEDF7)
private val light_surfaceContainerLow = Color(0xFFF2F3FD)
private val light_surfaceContainerLowest = Color(0xFFFFFFFF)
private val light_surfaceContainerHigh = Color(0xFFE6E8F1)
private val light_surfaceContainerHighest = Color(0xFFE0E2EC)

// ─── Dark tokens ─────────────────────────────────────────────────────────────

private val dark_primary = Color(0xFFAAC7FF)
private val dark_onPrimary = Color(0xFF001B3E)
private val dark_primaryContainer = Color(0xFF00458D)
private val dark_onPrimaryContainer = Color(0xFFD6E3FF)
private val dark_secondary = Color(0xFFAFC7F7)
private val dark_onSecondary = Color(0xFF001B3E)
private val dark_secondaryContainer = Color(0xFF2E4770)
private val dark_onSecondaryContainer = Color(0xFFD6E3FF)
private val dark_tertiary = Color(0xFFFFB68C)
private val dark_onTertiary = Color(0xFF321200)
private val dark_tertiaryContainer = Color(0xFF763400)
private val dark_onTertiaryContainer = Color(0xFFFFDBC9)
private val dark_error = Color(0xFFFFB4AB)
private val dark_onError = Color(0xFF690005)
private val dark_errorContainer = Color(0xFF93000A)
private val dark_onErrorContainer = Color(0xFFFFDAD6)
private val dark_background = Color(0xFF0F1117)
private val dark_onBackground = Color(0xFFE0E2EC)
private val dark_surface = Color(0xFF0F1117)
private val dark_onSurface = Color(0xFFE0E2EC)
private val dark_surfaceVariant = Color(0xFF414753)
private val dark_onSurfaceVariant = Color(0xFFC1C6D5)
private val dark_outline = Color(0xFF8B909E)
private val dark_outlineVariant = Color(0xFF414753)
private val dark_scrim = Color(0xFF000000)
private val dark_inverseSurface = Color(0xFFE0E2EC)
private val dark_inverseOnSurface = Color(0xFF2D3038)
private val dark_inversePrimary = Color(0xFF005AB4)
private val dark_surfaceTint = Color(0xFFAAC7FF)
private val dark_surfaceBright = Color(0xFF343641)
private val dark_surfaceDim = Color(0xFF0F1117)
private val dark_surfaceContainer = Color(0xFF1C1F27)
private val dark_surfaceContainerLow = Color(0xFF181C22)
private val dark_surfaceContainerLowest = Color(0xFF0A0D14)
private val dark_surfaceContainerHigh = Color(0xFF272A32)
private val dark_surfaceContainerHighest = Color(0xFF31353D)

// ─── Extended semantic colors ─────────────────────────────────────────────────

internal val light_profit = Color(0xFF2E7D32)
internal val light_onProfit = Color(0xFFFFFFFF)
internal val dark_profit = Color(0xFF66BB6A)
internal val dark_onProfit = Color(0xFF003909)

// ─── Color schemes ────────────────────────────────────────────────────────────

internal val DividoxLightColorScheme = lightColorScheme(
    primary = light_primary,
    onPrimary = light_onPrimary,
    primaryContainer = light_primaryContainer,
    onPrimaryContainer = light_onPrimaryContainer,
    secondary = light_secondary,
    onSecondary = light_onSecondary,
    secondaryContainer = light_secondaryContainer,
    onSecondaryContainer = light_onSecondaryContainer,
    tertiary = light_tertiary,
    onTertiary = light_onTertiary,
    tertiaryContainer = light_tertiaryContainer,
    onTertiaryContainer = light_onTertiaryContainer,
    error = light_error,
    onError = light_onError,
    errorContainer = light_errorContainer,
    onErrorContainer = light_onErrorContainer,
    background = light_background,
    onBackground = light_onBackground,
    surface = light_surface,
    onSurface = light_onSurface,
    surfaceVariant = light_surfaceVariant,
    onSurfaceVariant = light_onSurfaceVariant,
    outline = light_outline,
    outlineVariant = light_outlineVariant,
    scrim = light_scrim,
    inverseSurface = light_inverseSurface,
    inverseOnSurface = light_inverseOnSurface,
    inversePrimary = light_inversePrimary,
    surfaceTint = light_surfaceTint,
    surfaceBright = light_surfaceBright,
    surfaceDim = light_surfaceDim,
    surfaceContainer = light_surfaceContainer,
    surfaceContainerLow = light_surfaceContainerLow,
    surfaceContainerLowest = light_surfaceContainerLowest,
    surfaceContainerHigh = light_surfaceContainerHigh,
    surfaceContainerHighest = light_surfaceContainerHighest,
)

internal val DividoxDarkColorScheme = darkColorScheme(
    primary = dark_primary,
    onPrimary = dark_onPrimary,
    primaryContainer = dark_primaryContainer,
    onPrimaryContainer = dark_onPrimaryContainer,
    secondary = dark_secondary,
    onSecondary = dark_onSecondary,
    secondaryContainer = dark_secondaryContainer,
    onSecondaryContainer = dark_onSecondaryContainer,
    tertiary = dark_tertiary,
    onTertiary = dark_onTertiary,
    tertiaryContainer = dark_tertiaryContainer,
    onTertiaryContainer = dark_onTertiaryContainer,
    error = dark_error,
    onError = dark_onError,
    errorContainer = dark_errorContainer,
    onErrorContainer = dark_onErrorContainer,
    background = dark_background,
    onBackground = dark_onBackground,
    surface = dark_surface,
    onSurface = dark_onSurface,
    surfaceVariant = dark_surfaceVariant,
    onSurfaceVariant = dark_onSurfaceVariant,
    outline = dark_outline,
    outlineVariant = dark_outlineVariant,
    scrim = dark_scrim,
    inverseSurface = dark_inverseSurface,
    inverseOnSurface = dark_inverseOnSurface,
    inversePrimary = dark_inversePrimary,
    surfaceTint = dark_surfaceTint,
    surfaceBright = dark_surfaceBright,
    surfaceDim = dark_surfaceDim,
    surfaceContainer = dark_surfaceContainer,
    surfaceContainerLow = dark_surfaceContainerLow,
    surfaceContainerLowest = dark_surfaceContainerLowest,
    surfaceContainerHigh = dark_surfaceContainerHigh,
    surfaceContainerHighest = dark_surfaceContainerHighest,
)
