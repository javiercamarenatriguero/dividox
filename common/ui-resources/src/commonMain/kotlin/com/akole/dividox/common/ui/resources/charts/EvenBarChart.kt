package com.akole.dividox.common.ui.resources.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.ui.resources.theme.spacing
import kotlin.math.ceil
import kotlin.math.roundToInt

private const val Y_STEPS = 4
private const val DIMMED_ALPHA = 0.35f
private val NICE_STEP_FACTORS = floatArrayOf(1f, 2f, 5f, 10f)

/**
 * Bar chart that always fits its width: every bar gets the same slot, with the bar occupying
 * [barFraction] of it, so the gap between bars stays proportional whatever the number of entries.
 * Bars are capped at [maxBarWidth] (extra space is spread evenly) and X labels are thinned out
 * automatically so they never overlap. Tapping a bar highlights it and shows [popupLabelFormatter].
 */
@Composable
fun EvenBarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barHeight: Dp = 180.dp,
    maxBarWidth: Dp = 28.dp,
    barFraction: Float = 0.6f,
    popupLabelFormatter: ((BarChartEntry) -> String)? = null,
) {
    if (entries.isEmpty()) return
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelGap = MaterialTheme.spacing.xSmall
    var selected by remember(entries) { mutableStateOf<Int?>(null) }
    var popupSize by remember { mutableStateOf(IntSize.Zero) }
    val axisMax = remember(entries) { niceMax(entries.maxOf { it.value }) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val xLabelHeight = with(density) { textMeasurer.measure("0", labelStyle).size.height.toDp() }
        val totalHeight = barHeight + labelGap + xLabelHeight
        var layout by remember { mutableStateOf<BarLayout?>(null) }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
                .pointerInput(entries) {
                    detectTapGestures { tap ->
                        val index = layout?.indexAt(tap.x)
                        selected = if (index == selected) null else index
                    }
                },
        ) {
            val gutter = (0..Y_STEPS).maxOf {
                textMeasurer.measure(formatAxisValue(axisMax / Y_STEPS * it), labelStyle).size.width
            } + labelGap.toPx()
            val plotHeight = barHeight.toPx()
            val slot = (size.width - gutter) / entries.size
            val barWidth = minOf(slot * barFraction, maxBarWidth.toPx())
            layout = BarLayout(gutter, slot, entries.size)

            drawGridAndYAxis(textMeasurer, labelStyle, gridColor, gutter, plotHeight, axisMax)
            entries.forEachIndexed { i, entry ->
                val h = if (axisMax > 0f) plotHeight * (entry.value / axisMax) else 0f
                val left = gutter + slot * i + (slot - barWidth) / 2
                val alpha = if (selected == null || selected == i) 1f else DIMMED_ALPHA
                drawTopRoundedBar(barColor.copy(alpha = alpha), left, plotHeight - h, barWidth, h)
            }
            drawXLabels(textMeasurer, labelStyle, entries, gutter, slot, plotHeight + labelGap.toPx())
        }

        val index = selected
        if (index != null && popupLabelFormatter != null) {
            val entry = entries[index]
            Box(
                modifier = Modifier
                    .onSizeChanged { popupSize = it }
                    .offset {
                        val center = layout?.centerOf(index) ?: 0f
                        val maxX = (constraints.maxWidth - popupSize.width).coerceAtLeast(0)
                        IntOffset((center - popupSize.width / 2f).roundToInt().coerceIn(0, maxX), 0)
                    }
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .clickable { selected = null }
                    .padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.xSmall),
            ) {
                Text(
                    text = popupLabelFormatter(entry),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

private class BarLayout(val gutter: Float, val slot: Float, val count: Int) {
    fun indexAt(x: Float): Int? =
        if (x < gutter) null else ((x - gutter) / slot).toInt().coerceIn(0, count - 1)

    fun centerOf(index: Int): Float = gutter + slot * (index + 0.5f)
}

private fun DrawScope.drawGridAndYAxis(
    textMeasurer: TextMeasurer,
    style: TextStyle,
    gridColor: Color,
    gutter: Float,
    plotHeight: Float,
    axisMax: Float,
) {
    for (step in 0..Y_STEPS) {
        val y = plotHeight - plotHeight * step / Y_STEPS
        drawLine(gridColor, Offset(gutter, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        val label = textMeasurer.measure(formatAxisValue(axisMax / Y_STEPS * step), style)
        val labelY = (y - label.size.height / 2f).coerceIn(0f, plotHeight - label.size.height)
        drawText(label, topLeft = Offset(gutter - label.size.width - 4.dp.toPx(), labelY))
    }
}

private fun DrawScope.drawTopRoundedBar(color: Color, left: Float, top: Float, width: Float, height: Float) {
    if (height <= 0f) return
    val radius = minOf(width / 2, 6.dp.toPx(), height)
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = left,
                top = top,
                right = left + width,
                bottom = top + height,
                topLeftCornerRadius = CornerRadius(radius),
                topRightCornerRadius = CornerRadius(radius),
            ),
        )
    }
    drawPath(path, color)
}

private fun DrawScope.drawXLabels(
    textMeasurer: TextMeasurer,
    style: TextStyle,
    entries: List<BarChartEntry>,
    gutter: Float,
    slot: Float,
    top: Float,
) {
    val widest = entries.maxOf { textMeasurer.measure(it.label, style).size.width } + 6.dp.toPx()
    val stride = ceil(widest / slot).toInt().coerceAtLeast(1)
    // Anchor on the most recent bar so the latest period is always labelled.
    for (i in entries.indices.reversed() step stride) {
        val label = textMeasurer.measure(entries[i].label, style)
        val x = (gutter + slot * (i + 0.5f) - label.size.width / 2f)
            .coerceIn(gutter, size.width - label.size.width)
        drawText(label, topLeft = Offset(x, top))
    }
}

/** Rounds [max] up to a 1/2/5 × 10ⁿ step multiple so Y labels are readable. */
private fun niceMax(max: Float): Float {
    if (max <= 0f) return 1f
    val rawStep = max / Y_STEPS
    var magnitude = 1f
    while (magnitude * 10 <= rawStep) magnitude *= 10
    while (magnitude > rawStep) magnitude /= 10
    val step = NICE_STEP_FACTORS.map { it * magnitude }.first { it >= rawStep }
    return step * Y_STEPS
}

private fun formatAxisValue(value: Float): String = when {
    value >= 1_000_000f -> "${trimDecimal(value / 1_000_000f)}M"
    value >= 1_000f -> "${trimDecimal(value / 1_000f)}K"
    else -> trimDecimal(value)
}

private fun trimDecimal(value: Float): String {
    val rounded = (value * 10).roundToInt() / 10f
    return if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString()
}
