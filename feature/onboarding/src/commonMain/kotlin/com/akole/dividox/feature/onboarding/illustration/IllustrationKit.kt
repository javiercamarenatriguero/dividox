package com.akole.dividox.feature.onboarding.illustration

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.ui.resources.theme.spacing

internal val MOCK_CARD_WIDTH = 280.dp
private val MOCK_CARD_CORNER = 20.dp
private val MOCK_CARD_ELEVATION = 6.dp
private val AVATAR_SIZE = 32.dp
private val FLOAT_AMPLITUDE = 6.dp
private val SPARKLINE_STROKE = 2.dp
private val BAR_CORNER = 4.dp
private val OVERLAY_OVERLAP = 20.dp
private const val FLOAT_DURATION_MS = 2600
private const val BLOB_PRIMARY_ALPHA = 0.22f
private const val BLOB_SECONDARY_ALPHA = 0.12f
private const val BLOB_RADIUS_RATIO = 0.46f
private const val SOFT_BG_ALPHA = 0.14f
private const val AREA_FILL_ALPHA = 0.28f
private const val BAR_SPACING_RATIO = 0.45f
private const val DIMMED_BAR_ALPHA = 0.45f

/** Gentle vertical "breathing" float, used to make illustration elements feel alive. */
@Composable
internal fun Modifier.floating(
    durationMs: Int = FLOAT_DURATION_MS,
    amplitude: Dp = FLOAT_AMPLITUDE,
): Modifier {
    val transition = rememberInfiniteTransition(label = "float")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatProgress",
    )
    val amplitudePx = with(LocalDensity.current) { amplitude.toPx() }
    return graphicsLayer { translationY = progress * amplitudePx }
}

/** Opaque elevated backdrop so translucent pills stay readable over the stage blobs. */
@Composable
internal fun Modifier.surfaceBackdrop(): Modifier =
    shadow(MOCK_CARD_ELEVATION / 2, CircleShape).background(MaterialTheme.colorScheme.surface, CircleShape)

/** Main mock card with a floating [overlay] overlapping its bottom edge. */
@Composable
internal fun WithBottomOverlay(
    modifier: Modifier = Modifier,
    overlay: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        content()
        Box(modifier = Modifier.offset(y = -OVERLAY_OVERLAP)) { overlay() }
    }
}

/** Soft, theme-tinted blobs behind the illustration content. */
@Composable
internal fun IllustrationStage(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension * BLOB_RADIUS_RATIO
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(primary.copy(alpha = BLOB_PRIMARY_ALPHA), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
            )
            val smallCenter = Offset(size.width * 0.8f, size.height * 0.2f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(primary.copy(alpha = BLOB_SECONDARY_ALPHA), Color.Transparent),
                    center = smallCenter,
                    radius = radius / 2,
                ),
                radius = radius / 2,
                center = smallCenter,
            )
        }
        content()
    }
}

/** Stylised "app card" matching the real screens' surface cards. */
@Composable
internal fun MockCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(MOCK_CARD_CORNER),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = MOCK_CARD_ELEVATION,
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium), content = content)
    }
}

@Composable
internal fun MockLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
        maxLines = 1,
    )
}

/** Rounded ticker badge (stand-in for company logos). */
@Composable
internal fun TickerAvatar(ticker: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(AVATAR_SIZE)
            .clip(CircleShape)
            .background(color.copy(alpha = SOFT_BG_ALPHA)),
    ) {
        Text(
            text = ticker.take(2),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

/** Coloured pill, same visual language as the dashboard gain pill. */
@Composable
internal fun MockPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = SOFT_BG_ALPHA))
            .padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.xSmall),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(MaterialTheme.spacing.iconSmall))
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.xSmall))
        }
        Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}

/** Minimal line chart with a soft gradient area underneath. */
@Composable
internal fun Sparkline(
    points: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val stroke = SPARKLINE_STROKE.toPx()
        val min = points.min()
        val range = (points.max() - min).takeIf { it > 0f } ?: 1f
        val stepX = size.width / (points.size - 1)
        val line = Path()
        points.forEachIndexed { i, p ->
            val x = i * stepX
            val y = stroke + (1f - (p - min) / range) * (size.height - 2 * stroke)
            if (i == 0) line.moveTo(x, y) else line.lineTo(x, y)
        }
        if (filled) {
            val area = Path().apply {
                addPath(line)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(area, Brush.verticalGradient(listOf(color.copy(alpha = AREA_FILL_ALPHA), Color.Transparent)))
        }
        drawPath(line, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Rounded bar chart; the bar at [highlightIndex] is drawn in [highlightColor]. */
@Composable
internal fun MockBarChart(
    values: List<Float>,
    color: Color,
    highlightIndex: Int,
    highlightColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val max = values.max().takeIf { it > 0f } ?: 1f
        val slot = size.width / values.size
        val barWidth = slot * (1f - BAR_SPACING_RATIO)
        val corner = CornerRadius(BAR_CORNER.toPx())
        values.forEachIndexed { i, v ->
            val h = v / max * size.height
            drawRoundRect(
                color = if (i == highlightIndex) highlightColor else color.copy(alpha = DIMMED_BAR_ALPHA),
                topLeft = Offset(i * slot + (slot - barWidth) / 2, size.height - h),
                size = Size(barWidth, h),
                cornerRadius = corner,
            )
        }
    }
}
