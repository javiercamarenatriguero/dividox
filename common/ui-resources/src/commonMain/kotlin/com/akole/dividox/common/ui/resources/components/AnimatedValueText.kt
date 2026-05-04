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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

/**
 * A [Text] that animates transitions between values using a vertical ticker effect.
 * Designed for financial numbers: the outgoing value exits upward while the new
 * value enters from below, mimicking a counter or stock ticker.
 *
 * Use this wherever a numeric (or currency-formatted) value can change so that
 * currency symbol and converted amount are always animated together as a unit.
 *
 * @param autoShrink When true, the font size scales down automatically to keep
 *   the text on a single line, stopping at 60% of the original size.
 */
@Composable
fun AnimatedValueText(
    value: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    autoShrink: Boolean = false,
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn())
                .togetherWith(slideOutVertically { height -> -height } + fadeOut())
        },
        label = "AnimatedValueText",
    ) { animatedValue ->
        if (autoShrink) {
            ShrinkToFitText(
                text = animatedValue,
                modifier = modifier,
                style = style,
                color = color,
                fontWeight = fontWeight,
            )
        } else {
            Text(
                text = animatedValue,
                modifier = modifier,
                style = style,
                color = color,
                fontWeight = fontWeight,
            )
        }
    }
}

@Composable
private fun ShrinkToFitText(
    text: String,
    modifier: Modifier,
    style: TextStyle,
    color: Color,
    fontWeight: FontWeight?,
) {
    var fontScale by remember(text, style.fontSize) { mutableStateOf(1f) }
    var readyToDraw by remember(text, style.fontSize) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent { if (readyToDraw) drawContent() },
        style = style.copy(fontSize = style.fontSize * fontScale),
        color = color,
        fontWeight = fontWeight,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible,
        onTextLayout = { result ->
            if (result.didOverflowWidth && fontScale > 0.6f) {
                fontScale *= 0.9f
            } else {
                readyToDraw = true
            }
        },
    )
}

