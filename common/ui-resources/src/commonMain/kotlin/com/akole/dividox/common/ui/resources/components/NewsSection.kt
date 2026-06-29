package com.akole.dividox.common.ui.resources.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.news_section_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun NewsSection(
    news: List<NewsItemUi>,
    isLoading: Boolean,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (!isLoading && news.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.news_section_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.small),
        )

        if (isLoading) {
            NewsSectionPlaceholder(compact = compact)
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                news.forEach { item ->
                    NewsCard(item = item, compact = compact)
                }
            }
        }
    }
}

@Composable
private fun NewsSectionPlaceholder(compact: Boolean = false) {
    val transition = rememberInfiniteTransition(label = "news-shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "news-shimmer-alpha",
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        repeat(3) {
            OutlinedCard(
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha),
                ),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = Color.Transparent,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.small,
                        ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(MaterialTheme.spacing.medium)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha),
                            ),
                    )
                    if (!compact) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .height(MaterialTheme.spacing.small)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(
                                    MaterialTheme.colorScheme.outlineVariant
                                        .copy(alpha = alpha * 0.8f),
                                ),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(MaterialTheme.spacing.medium)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant
                                    .copy(alpha = alpha * 0.6f),
                            ),
                    )
                }
            }
        }
    }
}
