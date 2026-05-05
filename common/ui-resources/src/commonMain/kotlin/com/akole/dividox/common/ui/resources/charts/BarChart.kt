package com.akole.dividox.common.ui.resources.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp


import com.developerstring.jetco_kmp.charts.barchart.ColumnBarChart
import com.developerstring.jetco_kmp.charts.barchart.config.BarChartDefaults
import kotlin.math.roundToInt

data class BarChartEntry(
    val label: String,
    val value: Float,
)

/**
 * Generic bar chart composable.
 *
 * @param entries Data entries to display. [BarChartEntry.label] is shown on the X-axis;
 *   [BarChartEntry.value] determines bar height.
 * @param minBarSlotWidth Minimum width allocated per bar (including spacing). When the total
 *   required width exceeds the available container width, the chart scrolls horizontally.
 * @param skipAlternateXLabels When true, every other X-axis label is hidden to reduce crowding.
 *   Useful for month-level ranges with many bars.
 * @param popupLabelFormatter When provided, a tooltip is shown on bar tap at the tapped position
 *   using this formatter instead of the library's default numeric tooltip.
 * @param onBarClicked Optional callback invoked when a bar is tapped; receives the original entry.
 */
@Composable
fun BarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barHeight: Dp = 180.dp,
    barWidth: Dp = 22.dp,
    barShape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
    minBarSlotWidth: Dp = 48.dp,
    skipAlternateXLabels: Boolean = false,
    popupLabelFormatter: ((BarChartEntry) -> String)? = null,
    onBarClicked: ((BarChartEntry) -> Unit)? = null,
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val axisLabelStyle = MaterialTheme.typography.labelSmall.copy(color = onSurfaceVariant)
    var popupEntry: BarChartEntry? by remember { mutableStateOf(null) }
    var tapPosition by remember { mutableStateOf(Offset.Zero) }
    var popupSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(entries) {
        val current = popupEntry ?: return@LaunchedEffect
        popupEntry = entries.find { it.label == current.label }
    }

    val entryKeyMap: Map<String, BarChartEntry> = remember(entries, skipAlternateXLabels) {
        buildMap {
            entries.forEachIndexed { index, entry ->
                val reverseIndex = entries.size - 1 - index
                val key = if (skipAlternateXLabels && reverseIndex % 2 != 0) {
                    "\u200B".repeat(index + 1)
                } else {
                    entry.label
                }
                put(key, entry)
            }
        }
    }
    val chartData: Map<String, Float> = remember(entryKeyMap) {
        entryKeyMap.mapValues { (_, e) -> e.value }
    }

    BoxWithConstraints(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                tapPosition = down.position
            }
        },
    ) {
        // Ensure each bar gets at least minBarSlotWidth. When the total minimum
        // width exceeds the available container, the library's internal scroll kicks in.
        val minTotalWidth = minBarSlotWidth * entries.size
        val chartWidth = if (minTotalWidth > maxWidth) minTotalWidth else maxWidth

        ColumnBarChart(
            modifier = Modifier.width(chartWidth),
            chartData = chartData,
            barChartConfig = BarChartDefaults.columnBarChartConfig(
                color = barColor,
                height = barHeight,
                width = barWidth,
                shape = barShape,
            ),
            yAxisConfig = BarChartDefaults.yAxisConfig(
                isAxisScaleEnabled = true,
                textStyle = axisLabelStyle,
            ),
            xAxisConfig = BarChartDefaults.xAxisConfig(
                textStyle = axisLabelStyle,
            ),
            popUpConfig = BarChartDefaults.popUpConfig(
                enableBarPopUp = false,
                enableXAxisPopUp = false,
            ),
            gridLineStyle = BarChartDefaults.gridLineStyle(),
            enableAnimation = false,
            scrollEnable = minTotalWidth > maxWidth,
            onBarClicked = { (key, value) ->
                val original = entryKeyMap[key] ?: BarChartEntry(key, value)
                if (popupLabelFormatter != null) popupEntry = original
                onBarClicked?.invoke(original)
            },
        )

        popupEntry?.let { entry ->
            Box(
                modifier = Modifier
                    .onSizeChanged { popupSize = it }
                    .offset {
                        val containerWidthPx = maxWidth.toPx().roundToInt()
                        val rawX = (tapPosition.x - popupSize.width / 2f).roundToInt()
                        val clampedX = rawX.coerceIn(0, (containerWidthPx - popupSize.width).coerceAtLeast(0))
                        val rawY = (tapPosition.y - 72.dp.toPx()).roundToInt().coerceAtLeast(0)
                        IntOffset(x = clampedX, y = rawY)
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { popupEntry = null },
            ) {
                Text(
                    text = popupLabelFormatter!!(entry),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
