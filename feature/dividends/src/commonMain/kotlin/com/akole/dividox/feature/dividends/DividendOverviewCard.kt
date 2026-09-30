package com.akole.dividox.feature.dividends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.ui.resources.components.AnimatedValueText
import com.akole.dividox.common.ui.resources.format.formatPercentSigned
import com.akole.dividox.common.ui.resources.format.formatPrice
import com.akole.dividox.common.ui.resources.format.formatShort
import com.akole.dividox.common.ui.resources.theme.extendedColors
import com.akole.dividox.common.ui.resources.theme.spacing
import com.akole.dividox.integration.dividend.domain.model.DividendActivitySummary
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.dividends_metric_lifetime
import dividox.common.ui_resources.generated.resources.dividends_metric_next_payout
import dividox.common.ui_resources.generated.resources.dividends_metric_ytd
import dividox.common.ui_resources.generated.resources.dividends_yoy_caption
import dividox.common.ui_resources.generated.resources.ui_no_value
import org.jetbrains.compose.resources.stringResource

// Same visual tokens as the Dashboard's PortfolioOverviewCard, so both screens open with the same card.
private const val OVERVIEW_CORNER_DP = 24
private const val OVERVIEW_GRADIENT_ALPHA = 0.14f
private const val TILE_CORNER_DP = 16
private const val TILE_ICON_DP = 14
private const val TILE_BG_ALPHA = 0.7f
private const val PILL_BG_ALPHA = 0.14f

/**
 * Dividend overview: lifetime dividends as the hero number with the year-over-year change as a
 * coloured pill, followed by stat tiles (YTD, next payout, yield on cost vs. target).
 */
@Composable
internal fun DividendOverviewCard(
    summary: DividendActivitySummary,
    currency: Currency,
    modifier: Modifier = Modifier,
) {
    val cardBrush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = OVERVIEW_GRADIENT_ALPHA),
            MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(OVERVIEW_CORNER_DP.dp),
    ) {
        Column(
            modifier = Modifier
                .background(cardBrush)
                .padding(MaterialTheme.spacing.medium),
        ) {
            Text(
                text = stringResource(Res.string.dividends_metric_lifetime),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AnimatedValueText(
                value = summary.lifetime.formatPrice(currency),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                autoShrink = true,
            )
            summary.yoyPercent?.let { yoy ->
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    YoyPill(yoy)
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(
                        text = stringResource(Res.string.dividends_yoy_caption),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.height(IntrinsicSize.Min),
            ) {
                OverviewTile(
                    icon = Icons.Outlined.CalendarMonth,
                    label = stringResource(Res.string.dividends_metric_ytd),
                    value = summary.ytd.formatPrice(currency),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                val next = summary.nextPayout
                OverviewTile(
                    icon = Icons.Outlined.Event,
                    label = stringResource(Res.string.dividends_metric_next_payout),
                    value = next?.payment?.tickerId ?: stringResource(Res.string.ui_no_value),
                    caption = next?.payment?.paymentDate?.formatShort(),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun YoyPill(yoy: Double) {
    val color = if (yoy >= 0) MaterialTheme.extendedColors.profit else MaterialTheme.colorScheme.error
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = PILL_BG_ALPHA))
            .padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.xSmall),
    ) {
        Icon(
            imageVector = if (yoy < 0) Icons.AutoMirrored.Outlined.TrendingDown else Icons.AutoMirrored.Outlined.TrendingUp,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.xSmall))
        Text(
            text = yoy.formatPercentSigned(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun OverviewTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    val iconTint = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(TILE_CORNER_DP.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = TILE_BG_ALPHA))
            .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(MaterialTheme.spacing.iconMedium)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = PILL_BG_ALPHA)),
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(TILE_ICON_DP.dp))
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))
        AnimatedValueText(
            value = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            autoShrink = true,
        )
        if (caption != null) {
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
