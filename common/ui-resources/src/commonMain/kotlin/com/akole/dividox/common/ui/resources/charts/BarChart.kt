package com.akole.dividox.common.ui.resources.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.developerstring.jetco_kmp.charts.barchart.ColumnBarChart
import com.developerstring.jetco_kmp.charts.barchart.config.BarChartDefaults

data class BarChartEntry(
    val label: String,
    val value: Float,
)

@Composable
fun BarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barHeight: Dp = 180.dp,
    barWidth: Dp = 22.dp,
    onBarClicked: ((BarChartEntry) -> Unit)? = null,
) {
    val chartData: Map<String, Float> = entries.associate { it.label to it.value }

    ColumnBarChart(
        modifier = modifier,
        chartData = chartData,
        barChartConfig = BarChartDefaults.columnBarChartConfig(
            color = barColor,
            height = barHeight,
            width = barWidth,
        ),
        yAxisConfig = BarChartDefaults.yAxisConfig(isAxisScaleEnabled = false),
        xAxisConfig = BarChartDefaults.xAxisConfig(),
        popUpConfig = BarChartDefaults.popUpConfig(),
        gridLineStyle = BarChartDefaults.gridLineStyle(),
        enableAnimation = true,
        scrollEnable = true,
        onBarClicked = onBarClicked?.let { callback ->
            { (label, value) ->
                callback(BarChartEntry(label = label, value = value))
            }
        },
    )
}
