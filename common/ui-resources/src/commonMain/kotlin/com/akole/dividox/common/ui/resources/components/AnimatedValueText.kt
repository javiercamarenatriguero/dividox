package com.akole.dividox.common.ui.resources.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * A [Text] that animates transitions between values using a vertical ticker effect.
 * Designed for financial numbers: the outgoing value exits upward while the new
 * value enters from below, mimicking a counter or stock ticker.
 *
 * Use this wherever a numeric (or currency-formatted) value can change so that
 * currency symbol and converted amount are always animated together as a unit.
 */
@Composable
fun AnimatedValueText(
    value: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn())
                .togetherWith(slideOutVertically { height -> -height } + fadeOut())
        },
        label = "AnimatedValueText",
    ) { animatedValue ->
        Text(
            text = animatedValue,
            modifier = modifier,
            style = style,
            color = color,
            fontWeight = fontWeight,
        )
    }
}
