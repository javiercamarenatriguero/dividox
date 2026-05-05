package com.akole.dividox.common.ui.resources.charts

fun calculateChartFloor(min: Float, max: Float): Float {
    val range = max - min
    return (min - range * 0.1f).coerceAtLeast(0f)
}
