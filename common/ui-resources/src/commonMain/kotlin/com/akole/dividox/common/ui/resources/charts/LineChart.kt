package com.akole.dividox.common.ui.resources.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.developerstring.jetco_kmp.charts.linegraph.LineGraph
import com.developerstring.jetco_kmp.charts.linegraph.config.LineGraphDefaults
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

data class LineChartEntry(
    val label: String,
    val value: Float,
    val tooltipLabel: String = label,
)

/**
 * Generic line chart composable backed by jetco_kmp's [LineGraph].
 *
 * Supports drag-to-scrub: while a finger is on the chart, a dashed vertical
 * indicator and popup follow the closest data point. The library's tap popup
 * is suppressed while dragging so the two never overlap.
 *
 * @param entries Ordered data points.
 * @param height Height of the chart drawing area.
 * @param yAxisScaleCount Number of scale labels on the Y-axis.
 * @param yAxisValueOffset When non-zero, replaces the library's 0-based Y-axis
 *   with custom labels showing actual values (entry.value + offset).
 * @param popupFormatter Formatter used for both the drag scrubber and the
 *   library's tap popup. Receives the entry's label and (shifted) value.
 */
@Composable
fun LineChart(
    entries: List<LineChartEntry>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    yAxisScaleCount: Int = 4,
    yAxisValueOffset: Float = 0f,
    popupFormatter: ((label: String, value: Float) -> String)? = null,
) {
    if (entries.isEmpty()) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall

    val useCustomYAxis = yAxisValueOffset != 0f
    val maxEntryValue = entries.maxOf { it.value }.coerceAtLeast(0.0001f)
    val yAxisStep = if (yAxisScaleCount > 0) maxEntryValue / yAxisScaleCount else maxEntryValue
    val stepHeight = height / yAxisScaleCount

    var dragX: Float? by retain { mutableStateOf(null) }
    var yAxisWidthPx by retain { mutableIntStateOf(0) }
    var graphWidthPx by retain { mutableIntStateOf(0) }
    var popupWidthPx by retain { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val hoveredIndex: Int? by remember(entries.size) {
        derivedStateOf {
            val x = dragX ?: return@derivedStateOf null
            if (graphWidthPx <= 0 || entries.size < 2) return@derivedStateOf null
            val relX = x - yAxisWidthPx
            ((relX * (entries.size - 1)) / graphWidthPx.toFloat())
                .roundToInt()
                .coerceIn(0, entries.size - 1)
        }
    }
    // Stable boolean: only changes at gesture start/end, not on every pointer event.
    // Prevents LineGraph from recomposing while the finger is moving.
    val isDragging by retain(entries.size) { derivedStateOf { hoveredIndex != null } }

    val chartData = retain(entries) { entries.associate { it.label to it.value } }
    val lineConfig = retain(primaryColor) { LineGraphDefaults.lineConfig(lineColor = primaryColor) }
    val areaFillConfig = retain(primaryColor) { LineGraphDefaults.areaFillConfig(lineColor = primaryColor) }
    val pointConfig = retain { LineGraphDefaults.pointConfig(enabled = false) }
    val yAxisConfig = retain(useCustomYAxis, yAxisScaleCount, onSurfaceVariant) {
        LineGraphDefaults.yAxisConfig(
            isAxisScaleEnabled = !useCustomYAxis,
            axisScaleCount = yAxisScaleCount,
            textStyle = labelStyle.copy(color = onSurfaceVariant),
        )
    }
    val xAxisConfig = retain(onSurfaceVariant) {
        LineGraphDefaults.xAxisConfig(
            textStyle = labelStyle.copy(color = onSurfaceVariant),
        )
    }

    Box(
        modifier = modifier.pointerInput(entries) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                dragX = down.position.x
                drag(down.id) { change ->
                    change.consume()
                    dragX = change.position.x
                }
                dragX = null
            }
        },
    ) {
        Row {
            if (useCustomYAxis) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .onSizeChanged { yAxisWidthPx = it.width }
                        .padding(end = 4.dp),
                ) {
                    repeat(yAxisScaleCount + 1) { index ->
                        val barScale = yAxisScaleCount - index
                        val actualValue = barScale * yAxisStep + yAxisValueOffset
                        Row(
                            modifier = Modifier.height(stepHeight),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(
                                text = formatYAxisLabel(actualValue),
                                style = labelStyle,
                                color = onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .let { if (useCustomYAxis) it.weight(1f) else it.fillMaxWidth() }
                    .onSizeChanged { graphWidthPx = it.width },
            ) {
                LineGraph(
                    modifier = Modifier.fillMaxWidth(),
                    chartData = chartData,
                    chartHeight = height,
                    lineConfig = lineConfig,
                    areaFillConfig = areaFillConfig,
                    pointConfig = pointConfig,
                    yAxisConfig = yAxisConfig,
                    xAxisConfig = xAxisConfig,
                    popUpConfig = LineGraphDefaults.popUpConfig(
                        enabled = !isDragging,
                        background = primaryContainer,
                    ),
                    customPopup = if (!isDragging) {
                        popupFormatter?.let { formatter ->
                            { label, value, onDismiss ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = primaryContainer,
                                    onClick = onDismiss,
                                ) {
                                    Text(
                                        text = formatter(label, value),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = onPrimaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 4.dp,
                                        ),
                                    )
                                }
                            }
                        }
                    } else {
                        null
                    },
                    autoShrinkXAxisLabels = true,
                )
            }
        }

        // Drag scrubber overlay
        hoveredIndex?.let { idx ->
            if (graphWidthPx <= 0 || entries.size < 2 || idx >= entries.size) return@let
            val entry = entries[idx]
            val spacingPx = graphWidthPx.toFloat() / (entries.size - 1)
            val lineXPx = yAxisWidthPx + idx * spacingPx
            val totalWidthPx = yAxisWidthPx + graphWidthPx

            Canvas(modifier = Modifier.matchParentSize()) {
                val topPaddingPx = stepHeight.toPx()
                val chartHeightPx = height.toPx()
                val normalizedY = (entry.value / maxEntryValue).coerceIn(0f, 1f)
                val dotYPx = topPaddingPx + (1f - normalizedY) * chartHeightPx

                // Dashed vertical indicator
                drawLine(
                    color = primaryColor.copy(alpha = 0.5f),
                    start = Offset(lineXPx, topPaddingPx),
                    end = Offset(lineXPx, topPaddingPx + chartHeightPx),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
                    ),
                )
                // Outer ring
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(lineXPx, dotYPx),
                )
                // Inner fill
                drawCircle(
                    color = primaryContainer,
                    radius = 2.dp.toPx(),
                    center = Offset(lineXPx, dotYPx),
                )
            }

            if (popupFormatter != null) {
                Box(
                    modifier = Modifier
                        .onSizeChanged { popupWidthPx = it.width }
                        .offset(
                            x = with(density) {
                                (lineXPx - popupWidthPx / 2f)
                                    .roundToInt()
                                    .coerceIn(0, (totalWidthPx - popupWidthPx).coerceAtLeast(0))
                                    .toDp()
                            },
                            y = 4.dp,
                        )
                        .background(
                            color = primaryContainer,
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = popupFormatter(entry.tooltipLabel, entry.value),
                        style = MaterialTheme.typography.bodySmall,
                        color = onPrimaryContainer,
                    )
                }
            }
        }
    }
}

private fun formatYAxisLabel(value: Float): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    val negative = value < 0f
    val absVal = abs(value)
    val prefix = if (negative) "-" else ""
    return when {
        absVal >= 1_000_000f -> "$prefix${formatDecimalShort(absVal / 1_000_000f)}M"
        absVal >= 1_000f -> "$prefix${formatDecimalShort(absVal / 1_000f)}K"
        else -> "$prefix${formatDecimalShort(absVal)}"
    }
}

private fun formatDecimalShort(value: Float): String {
    val absRounded = round(abs(value) * 100).toLong()
    val intPart = absRounded / 100
    val decPart = (absRounded % 100).toInt()
    return when {
        decPart == 0 -> "$intPart"
        decPart % 10 == 0 -> "$intPart.${decPart / 10}"
        else -> "$intPart.${decPart.toString().padStart(2, '0')}"
    }
}
