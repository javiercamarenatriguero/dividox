package com.akole.dividox.feature.onboarding.illustration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.ui.resources.theme.extendedColors
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.dividends_metric_next_payout
import dividox.common.ui_resources.generated.resources.dividends_section_projection
import dividox.common.ui_resources.generated.resources.metric_total_value
import org.jetbrains.compose.resources.stringResource

/** Illustrative sample data (proper nouns / figures, not translatable copy). */
internal object OnboardingSamples {
    const val TOTAL_VALUE = "24.350,00 €"
    const val TOTAL_VALUE_USD = "≈ 26.410,00 $"
    const val DAY_GAIN = "+312 €  +1,3 %"
    const val DAY_GAIN_SHORT = "+312 €"
    const val YEARLY_DIVIDENDS = "1.284 €"
    const val NEXT_PAYOUT = "KO · 0,51 €"
    const val NEXT_PAYOUT_DATE = "15/10"
    val PORTFOLIO_TREND = listOf(3f, 4f, 3.6f, 5f, 4.7f, 6.2f, 5.8f, 7f, 6.6f, 8.2f, 8f, 9.4f)
    val DIVIDEND_MONTHS = listOf(3f, 5f, 8f, 4f, 6f, 9f, 3f, 5f, 7f, 10f, 4f, 6f)
    const val DIVIDEND_HIGHLIGHT_MONTH = 9
}

private data class SampleHolding(val ticker: String, val name: String, val change: String, val isUp: Boolean)

private val SAMPLE_HOLDINGS = listOf(
    SampleHolding("AAPL", "Apple", "+2,1 %", isUp = true),
    SampleHolding("KO", "Coca-Cola", "+0,8 %", isUp = true),
    SampleHolding("SAN", "Santander", "−0,4 %", isUp = false),
)

private val SPARKLINE_HEIGHT = 56.dp
private val BAR_CHART_HEIGHT = 72.dp
private val FLOATING_OFFSET = 18.dp

@Composable
internal fun PortfolioIllustration(modifier: Modifier = Modifier) {
    val profit = MaterialTheme.extendedColors.profit
    IllustrationStage(modifier) {
        Box(modifier = Modifier.floating(durationMs = 3200)) {
            MockCard(modifier = Modifier.width(MOCK_CARD_WIDTH)) {
                MockLabel(stringResource(Res.string.metric_total_value))
                Text(
                    text = OnboardingSamples.TOTAL_VALUE,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))
                MockPill(OnboardingSamples.DAY_GAIN, profit, icon = Icons.AutoMirrored.Outlined.TrendingUp)
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Sparkline(
                    points = OnboardingSamples.PORTFOLIO_TREND,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().height(SPARKLINE_HEIGHT),
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                    SAMPLE_HOLDINGS.forEach { HoldingRow(it) }
                }
            }
            MockPill(
                text = OnboardingSamples.DAY_GAIN_SHORT,
                color = profit,
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = FLOATING_OFFSET, y = -FLOATING_OFFSET)
                    .floating(durationMs = 2200)
                    .surfaceBackdrop(),
            )
        }
    }
}

@Composable
private fun HoldingRow(holding: SampleHolding) {
    val color = if (holding.isUp) MaterialTheme.extendedColors.profit else MaterialTheme.colorScheme.error
    Row(verticalAlignment = Alignment.CenterVertically) {
        TickerAvatar(holding.ticker, MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
        Column(modifier = Modifier.weight(1f)) {
            Text(holding.ticker, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(
                holding.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = if (holding.isUp) {
                Icons.AutoMirrored.Outlined.TrendingUp
            } else {
                Icons.AutoMirrored.Outlined.TrendingDown
            },
            contentDescription = null,
            tint = color,
            modifier = Modifier.width(MaterialTheme.spacing.iconSmall),
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.xSmall))
        Text(holding.change, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
internal fun DividendsIllustration(modifier: Modifier = Modifier) {
    IllustrationStage(modifier) {
        WithBottomOverlay(
            modifier = Modifier.floating(durationMs = 3000),
            overlay = { NextPayoutCard(modifier = Modifier.floating(durationMs = 2300)) },
        ) { DividendsCard() }
        CoinBadge(
            symbol = "€",
            modifier = Modifier.align(Alignment.TopEnd).offset(x = -FLOATING_OFFSET * 2, y = FLOATING_OFFSET)
                .floating(durationMs = 1900),
        )
        CoinBadge(
            symbol = "$",
            modifier = Modifier.align(Alignment.TopStart).offset(x = FLOATING_OFFSET * 2, y = FLOATING_OFFSET * 2)
                .floating(durationMs = 2600),
        )
    }
}

@Composable
private fun DividendsCard() {
    MockCard(modifier = Modifier.width(MOCK_CARD_WIDTH)) {
        MockLabel(stringResource(Res.string.dividends_section_projection))
        Text(
            text = OnboardingSamples.YEARLY_DIVIDENDS,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        MockBarChart(
            values = OnboardingSamples.DIVIDEND_MONTHS,
            color = MaterialTheme.colorScheme.primary,
            highlightIndex = OnboardingSamples.DIVIDEND_HIGHLIGHT_MONTH,
            highlightColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().height(BAR_CHART_HEIGHT),
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
    }
}

@Composable
private fun NextPayoutCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(MaterialTheme.spacing.medium),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = MaterialTheme.spacing.small,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
        ) {
            Icon(Icons.Outlined.Event, contentDescription = null, modifier = Modifier.width(MaterialTheme.spacing.iconMedium))
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Column {
                Text(stringResource(Res.string.dividends_metric_next_payout), style = MaterialTheme.typography.labelSmall)
                Text(OnboardingSamples.NEXT_PAYOUT, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
            Text(OnboardingSamples.NEXT_PAYOUT_DATE, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Round "coin" with a currency symbol. */
@Composable
internal fun CoinBadge(symbol: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.tertiary) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = color,
        shadowElevation = MaterialTheme.spacing.xSmall,
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.small + MaterialTheme.spacing.xSmall,
                vertical = MaterialTheme.spacing.small,
            ),
        )
    }
}
