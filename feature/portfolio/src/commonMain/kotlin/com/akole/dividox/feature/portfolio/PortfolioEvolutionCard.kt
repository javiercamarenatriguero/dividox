package com.akole.dividox.feature.portfolio

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.ui.resources.format.formatPercentSigned
import com.akole.dividox.common.ui.resources.format.formatPrice
import com.akole.dividox.common.ui.resources.format.formatPriceSigned
import com.akole.dividox.common.ui.resources.format.formatShort
import com.akole.dividox.common.ui.resources.theme.extendedColors
import com.akole.dividox.common.ui.resources.theme.spacing
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.integration.security.domain.model.PortfolioValuePoint
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.portfolio_evolution_1m
import dividox.common.ui_resources.generated.resources.portfolio_evolution_1y
import dividox.common.ui_resources.generated.resources.portfolio_evolution_5y
import dividox.common.ui_resources.generated.resources.portfolio_evolution_caption
import dividox.common.ui_resources.generated.resources.portfolio_evolution_title
import dividox.common.ui_resources.generated.resources.ui_no_value
import org.jetbrains.compose.resources.stringResource

private const val CARD_CORNER_DP = 16
private const val CHIP_CORNER_DP = 8
private const val SPARKLINE_HEIGHT_DP = 56
private const val LINE_WIDTH_DP = 2
private const val FILL_ALPHA = 0.22f
private const val LOADING_ALPHA = 0.45f

/**
 * Compact sparkline of the portfolio value over the selected period. Dragging horizontally scrubs the
 * curve and shows the value on that date in the header.
 */
@Composable
internal fun PortfolioEvolutionCard(
    points: List<PortfolioValuePoint>,
    isLoading: Boolean,
    period: ChartPeriod,
    periods: List<ChartPeriod>,
    currency: Currency,
    onPeriodSelected: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    var scrubIndex by remember(points) { mutableStateOf<Int?>(null) }
    val contentAlpha by animateFloatAsState(if (isLoading) LOADING_ALPHA else 1f)
    val first = points.firstOrNull()?.value ?: 0.0
    val last = points.lastOrNull()?.value ?: 0.0
    val isUp = last >= first
    val lineColor = if (isUp) MaterialTheme.extendedColors.profit else MaterialTheme.colorScheme.error

    Surface(
        shape = RoundedCornerShape(CARD_CORNER_DP.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Row(verticalAlignment = Alignment.Top) {
                EvolutionHeader(
                    points = points,
                    scrubIndex = scrubIndex,
                    currency = currency,
                    changeColor = lineColor,
                    modifier = Modifier.weight(1f).alpha(contentAlpha),
                )
                PeriodSelector(period = period, periods = periods, onPeriodSelected = onPeriodSelected)
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Sparkline(
                values = points.map { it.value },
                color = lineColor,
                scrubIndex = scrubIndex,
                onScrub = { scrubIndex = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SPARKLINE_HEIGHT_DP.dp)
                    .alpha(contentAlpha),
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))
            Text(
                text = stringResource(Res.string.portfolio_evolution_caption),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EvolutionHeader(
    points: List<PortfolioValuePoint>,
    scrubIndex: Int?,
    currency: Currency,
    changeColor: Color,
    modifier: Modifier = Modifier,
) {
    val first = points.firstOrNull()?.value ?: 0.0
    val scrubbed = scrubIndex?.let { points.getOrNull(it) }
    Column(modifier = modifier) {
        Text(
            text = scrubbed?.date?.formatShort() ?: stringResource(Res.string.portfolio_evolution_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (scrubbed != null) {
            Text(
                text = scrubbed.value.formatPrice(currency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        } else if (points.size >= 2) {
            val change = points.last().value - first
            val changePct = if (first > 0.0) change / first * 100.0 else 0.0
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = changePct.formatPercentSigned(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = changeColor,
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = change.formatPriceSigned(currency),
                    style = MaterialTheme.typography.bodySmall,
                    color = changeColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            Text(
                text = stringResource(Res.string.ui_no_value),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PeriodSelector(
    period: ChartPeriod,
    periods: List<ChartPeriod>,
    onPeriodSelected: (ChartPeriod) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall)) {
        periods.forEach { option ->
            val selected = option == period
            Surface(
                shape = RoundedCornerShape(CHIP_CORNER_DP.dp),
                color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                modifier = Modifier.clickable(enabled = !selected) { onPeriodSelected(option) },
            ) {
                Text(
                    text = option.evolutionLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.small,
                        vertical = MaterialTheme.spacing.xSmall,
                    ),
                )
            }
        }
    }
}

@Composable
private fun Sparkline(
    values: List<Double>,
    color: Color,
    scrubIndex: Int?,
    onScrub: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val baselineColor = MaterialTheme.colorScheme.outlineVariant
    val scrubColor = MaterialTheme.colorScheme.onSurfaceVariant
    val count = values.size
    Canvas(
        modifier = modifier.pointerInput(count) {
            if (count < 2) return@pointerInput
            fun indexAt(x: Float) = ((x / size.width) * (count - 1)).toInt().coerceIn(0, count - 1)
            detectHorizontalDragGestures(
                onDragStart = { onScrub(indexAt(it.x)) },
                onDragEnd = { onScrub(null) },
                onDragCancel = { onScrub(null) },
                onHorizontalDrag = { change, _ -> onScrub(indexAt(change.position.x)) },
            )
        },
    ) {
        if (count < 2) {
            drawLine(
                color = baselineColor,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = LINE_WIDTH_DP.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )
            return@Canvas
        }
        val min = values.min()
        val range = (values.max() - min).takeIf { it > 0.0 } ?: 1.0
        val stroke = LINE_WIDTH_DP.dp.toPx()
        val usableHeight = size.height - stroke
        fun x(i: Int) = i * size.width / (count - 1)
        fun y(v: Double) = (stroke / 2 + usableHeight * (1 - (v - min) / range)).toFloat()

        val line = Path().apply {
            moveTo(x(0), y(values[0]))
            for (i in 1 until count) lineTo(x(i), y(values[i]))
        }
        val fill = Path().apply {
            addPath(line)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = FILL_ALPHA), Color.Transparent)))
        drawPath(line, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

        scrubIndex?.let { i ->
            val px = x(i)
            drawLine(scrubColor, Offset(px, 0f), Offset(px, size.height), strokeWidth = 1.dp.toPx())
            drawCircle(color, radius = stroke * 2, center = Offset(px, y(values[i])))
        }
    }
}

@Composable
private fun ChartPeriod.evolutionLabel(): String = stringResource(
    when (this) {
        ChartPeriod.ONE_MONTH -> Res.string.portfolio_evolution_1m
        ChartPeriod.FIVE_YEARS -> Res.string.portfolio_evolution_5y
        else -> Res.string.portfolio_evolution_1y
    },
)
