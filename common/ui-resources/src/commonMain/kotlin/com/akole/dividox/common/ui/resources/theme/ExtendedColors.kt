package com.akole.dividox.common.ui.resources.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class DividoxExtendedColors(
    val profit: Color,
    val onProfit: Color,
)

internal val LightExtendedColors = DividoxExtendedColors(
    profit = light_profit,
    onProfit = light_onProfit,
)

internal val DarkExtendedColors = DividoxExtendedColors(
    profit = dark_profit,
    onProfit = dark_onProfit,
)

internal val LocalDividoxExtendedColors = staticCompositionLocalOf {
    DividoxExtendedColors(profit = Color.Unspecified, onProfit = Color.Unspecified)
}
