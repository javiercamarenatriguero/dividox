package com.akole.dividox.common.ui.resources.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.news_cd_published_time
import dividox.common.ui_resources.generated.resources.news_time_days_ago
import dividox.common.ui_resources.generated.resources.news_time_hours_ago
import dividox.common.ui_resources.generated.resources.news_time_minutes_ago
import kotlin.math.abs
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource

@Composable
fun NewsCard(
    item: NewsItemUi,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    OutlinedCard(
        onClick = { runCatching { uriHandler.openUri(item.link) } },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.small,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
        ) {
            Text(
                text = item.title,
                style = if (compact) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.titleSmall
                },
                fontWeight = if (compact) FontWeight.Medium else FontWeight.SemiBold,
                maxLines = if (compact) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!compact && item.summary != null) {
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Text(
                        text = item.publisher,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.xSmall,
                        ),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = stringResource(Res.string.news_cd_published_time),
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.xSmall))
                Text(
                    text = relativeTimeText(item.publishedAtEpochSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun relativeTimeText(epochSeconds: Long): String {
    val nowSeconds = Clock.System.now().toEpochMilliseconds() / 1000
    val diff = abs(nowSeconds - epochSeconds)
    return when {
        diff < 3600 -> stringResource(Res.string.news_time_minutes_ago, (diff / 60).toInt())
        diff < 86400 -> stringResource(Res.string.news_time_hours_ago, (diff / 3600).toInt())
        else -> stringResource(Res.string.news_time_days_ago, (diff / 86400).toInt())
    }
}
