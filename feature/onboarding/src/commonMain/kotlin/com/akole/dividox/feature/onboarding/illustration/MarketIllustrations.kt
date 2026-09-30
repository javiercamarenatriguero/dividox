package com.akole.dividox.feature.onboarding.illustration

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.ui.resources.theme.extendedColors
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.dashboard_market_indices_title
import dividox.common.ui_resources.generated.resources.metric_total_value
import dividox.common.ui_resources.generated.resources.onboarding_sample_search_query
import dividox.common.ui_resources.generated.resources.section_favourites
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class SampleIndex(val name: String, val change: String, val isUp: Boolean, val trend: List<Float>)

private val SAMPLE_INDICES = listOf(
    SampleIndex("S&P 500", "+0,9 %", isUp = true, trend = listOf(2f, 3f, 2.5f, 4f, 3.8f, 5f, 5.6f)),
    SampleIndex("IBEX 35", "+0,4 %", isUp = true, trend = listOf(3f, 2.6f, 3.4f, 3.1f, 4f, 3.9f, 4.4f)),
    SampleIndex("DAX", "−0,6 %", isUp = false, trend = listOf(5f, 4.6f, 4.9f, 4f, 3.6f, 3.8f, 3f)),
)

private data class SampleResult(val ticker: String, val name: String, val exchange: String, val isFavorite: Boolean)

private val SAMPLE_RESULTS = listOf(
    SampleResult("KO", "Coca-Cola Co.", "NYSE", isFavorite = true),
    SampleResult("CCEP", "Coca-Cola Europacific", "NASDAQ", isFavorite = false),
    SampleResult("KOF", "Coca-Cola FEMSA", "NYSE", isFavorite = false),
)

private const val TOP_GAINER = "NVDA +4,2 %"
private const val TOP_LOSER = "INTC −3,1 %"
private val ORBIT_SYMBOLS = listOf("€", "$", "£", "¥", "₣", "₩")

private val INDEX_SPARK_WIDTH = 64.dp
private val INDEX_SPARK_HEIGHT = 24.dp
private val FLOATING_OFFSET = 18.dp
private val CURSOR_WIDTH = 2.dp
private val ORBIT_RADIUS = 130.dp
private const val ORBIT_Y_SQUASH = 0.62f
private const val ORBIT_DURATION_MS = 24_000
private const val CURSOR_BLINK_MS = 530
private const val FULL_TURN_DEG = 360f

@Composable
internal fun MarketIllustration(modifier: Modifier = Modifier) {
    IllustrationStage(modifier) {
        WithBottomOverlay(
            modifier = Modifier.floating(durationMs = 3100),
            overlay = { TopMoversPills() },
        ) {
            MockCard(modifier = Modifier.width(MOCK_CARD_WIDTH)) {
                MockLabel(stringResource(Res.string.dashboard_market_indices_title))
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                    SAMPLE_INDICES.forEach { IndexRow(it) }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            }
        }
    }
}

@Composable
private fun TopMoversPills() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = Modifier.floating(durationMs = 2300),
    ) {
        MockPill(
            text = TOP_GAINER,
            color = MaterialTheme.extendedColors.profit,
            icon = Icons.AutoMirrored.Outlined.TrendingUp,
            modifier = Modifier.surfaceBackdrop(),
        )
        MockPill(
            text = TOP_LOSER,
            color = MaterialTheme.colorScheme.error,
            icon = Icons.AutoMirrored.Outlined.TrendingDown,
            modifier = Modifier.surfaceBackdrop(),
        )
    }
}

@Composable
private fun IndexRow(index: SampleIndex) {
    val color = if (index.isUp) MaterialTheme.extendedColors.profit else MaterialTheme.colorScheme.error
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = index.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Sparkline(
            points = index.trend,
            color = color,
            filled = false,
            modifier = Modifier.width(INDEX_SPARK_WIDTH).height(INDEX_SPARK_HEIGHT),
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
        MockPill(text = index.change, color = color)
    }
}

@Composable
internal fun SearchIllustration(modifier: Modifier = Modifier) {
    IllustrationStage(modifier) {
        Box(modifier = Modifier.floating(durationMs = 3000)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(MOCK_CARD_WIDTH),
            ) {
                MockSearchBar()
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                MockCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                        SAMPLE_RESULTS.forEach { ResultRow(it) }
                    }
                }
            }
            MockPill(
                text = stringResource(Res.string.section_favourites),
                color = MaterialTheme.colorScheme.error,
                icon = Icons.Filled.Favorite,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = FLOATING_OFFSET / 2, y = FLOATING_OFFSET)
                    .floating(durationMs = 2100)
                    .surfaceBackdrop(),
            )
        }
    }
}

@Composable
private fun MockSearchBar() {
    val transition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(CURSOR_BLINK_MS, easing = LinearEasing), RepeatMode.Reverse),
        label = "cursorAlpha",
    )
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = MaterialTheme.spacing.xSmall,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(stringResource(Res.string.onboarding_sample_search_query), style = MaterialTheme.typography.bodyLarge)
            Box(
                modifier = Modifier
                    .width(CURSOR_WIDTH)
                    .height(MaterialTheme.spacing.iconSmall)
                    .graphicsLayer { alpha = cursorAlpha }
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun ResultRow(result: SampleResult) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TickerAvatar(result.ticker, MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
        Column(modifier = Modifier.weight(1f)) {
            Text(result.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                "${result.ticker} · ${result.exchange}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = if (result.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = if (result.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun CurrencyIllustration(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "orbit")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN_DEG,
        animationSpec = infiniteRepeatable(tween(ORBIT_DURATION_MS, easing = LinearEasing)),
        label = "orbitAngle",
    )
    val radiusPx = with(LocalDensity.current) { ORBIT_RADIUS.toPx() }
    IllustrationStage(modifier) {
        ORBIT_SYMBOLS.forEachIndexed { i, symbol ->
            CoinBadge(
                symbol = symbol,
                color = if (i % 2 == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.graphicsLayer {
                    val rad = (angle + i * FULL_TURN_DEG / ORBIT_SYMBOLS.size) * PI.toFloat() / (FULL_TURN_DEG / 2)
                    translationX = cos(rad) * radiusPx
                    translationY = sin(rad) * radiusPx * ORBIT_Y_SQUASH
                },
            )
        }
        MockCard(modifier = Modifier.floating(durationMs = 3000)) {
            MockLabel(stringResource(Res.string.metric_total_value))
            Text(
                text = OnboardingSamples.TOTAL_VALUE,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = OnboardingSamples.TOTAL_VALUE_USD,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shadowElevation = MaterialTheme.spacing.xSmall,
            modifier = Modifier.align(Alignment.BottomCenter).offset(y = -FLOATING_OFFSET).floating(durationMs = 2200),
        ) {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = null,
                modifier = Modifier.padding(MaterialTheme.spacing.small).size(MaterialTheme.spacing.iconMedium),
            )
        }
    }
}
