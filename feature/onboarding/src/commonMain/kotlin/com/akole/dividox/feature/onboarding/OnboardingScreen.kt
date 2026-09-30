package com.akole.dividox.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.common.ui.resources.theme.spacing
import com.akole.dividox.feature.onboarding.illustration.CurrencyIllustration
import com.akole.dividox.feature.onboarding.illustration.DividendsIllustration
import com.akole.dividox.feature.onboarding.illustration.MarketIllustration
import com.akole.dividox.feature.onboarding.illustration.PortfolioIllustration
import com.akole.dividox.feature.onboarding.illustration.SearchIllustration
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.onboarding_cd_illustration
import dividox.common.ui_resources.generated.resources.onboarding_next
import dividox.common.ui_resources.generated.resources.onboarding_page1_feature1
import dividox.common.ui_resources.generated.resources.onboarding_page1_feature2
import dividox.common.ui_resources.generated.resources.onboarding_page1_feature3
import dividox.common.ui_resources.generated.resources.onboarding_page1_subtitle
import dividox.common.ui_resources.generated.resources.onboarding_page1_title
import dividox.common.ui_resources.generated.resources.onboarding_page2_feature1
import dividox.common.ui_resources.generated.resources.onboarding_page2_feature2
import dividox.common.ui_resources.generated.resources.onboarding_page2_feature3
import dividox.common.ui_resources.generated.resources.onboarding_page2_subtitle
import dividox.common.ui_resources.generated.resources.onboarding_page2_title
import dividox.common.ui_resources.generated.resources.onboarding_page3_feature1
import dividox.common.ui_resources.generated.resources.onboarding_page3_feature2
import dividox.common.ui_resources.generated.resources.onboarding_page3_feature3
import dividox.common.ui_resources.generated.resources.onboarding_page3_subtitle
import dividox.common.ui_resources.generated.resources.onboarding_page3_title
import dividox.common.ui_resources.generated.resources.onboarding_page4_feature1
import dividox.common.ui_resources.generated.resources.onboarding_page4_feature2
import dividox.common.ui_resources.generated.resources.onboarding_page4_feature3
import dividox.common.ui_resources.generated.resources.onboarding_page4_subtitle
import dividox.common.ui_resources.generated.resources.onboarding_page4_title
import dividox.common.ui_resources.generated.resources.onboarding_page5_feature1
import dividox.common.ui_resources.generated.resources.onboarding_page5_feature2
import dividox.common.ui_resources.generated.resources.onboarding_page5_feature3
import dividox.common.ui_resources.generated.resources.onboarding_page5_subtitle
import dividox.common.ui_resources.generated.resources.onboarding_page5_title
import dividox.common.ui_resources.generated.resources.onboarding_skip
import dividox.common.ui_resources.generated.resources.onboarding_start
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.absoluteValue

private data class OnboardingPage(
    val title: StringResource,
    val subtitle: StringResource,
    val features: List<StringResource>,
    val illustration: @Composable (Modifier) -> Unit,
)

private val ONBOARDING_PAGES = listOf(
    OnboardingPage(
        title = Res.string.onboarding_page1_title,
        subtitle = Res.string.onboarding_page1_subtitle,
        features = listOf(
            Res.string.onboarding_page1_feature1,
            Res.string.onboarding_page1_feature2,
            Res.string.onboarding_page1_feature3,
        ),
        illustration = { PortfolioIllustration(it) },
    ),
    OnboardingPage(
        title = Res.string.onboarding_page2_title,
        subtitle = Res.string.onboarding_page2_subtitle,
        features = listOf(
            Res.string.onboarding_page2_feature1,
            Res.string.onboarding_page2_feature2,
            Res.string.onboarding_page2_feature3,
        ),
        illustration = { DividendsIllustration(it) },
    ),
    OnboardingPage(
        title = Res.string.onboarding_page3_title,
        subtitle = Res.string.onboarding_page3_subtitle,
        features = listOf(
            Res.string.onboarding_page3_feature1,
            Res.string.onboarding_page3_feature2,
            Res.string.onboarding_page3_feature3,
        ),
        illustration = { MarketIllustration(it) },
    ),
    OnboardingPage(
        title = Res.string.onboarding_page4_title,
        subtitle = Res.string.onboarding_page4_subtitle,
        features = listOf(
            Res.string.onboarding_page4_feature1,
            Res.string.onboarding_page4_feature2,
            Res.string.onboarding_page4_feature3,
        ),
        illustration = { SearchIllustration(it) },
    ),
    OnboardingPage(
        title = Res.string.onboarding_page5_title,
        subtitle = Res.string.onboarding_page5_subtitle,
        features = listOf(
            Res.string.onboarding_page5_feature1,
            Res.string.onboarding_page5_feature2,
            Res.string.onboarding_page5_feature3,
        ),
        illustration = { CurrencyIllustration(it) },
    ),
)

private val INDICATOR_DOT = 8.dp
private val INDICATOR_ACTIVE_WIDTH = 24.dp
private val FEATURE_ICON = 14.dp
private const val BACKGROUND_TINT_ALPHA = 0.08f
private const val MIN_PAGE_ALPHA = 0.3f
private const val MIN_PAGE_SCALE = 0.88f

@Composable
fun OnboardingScreen(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = ONBOARDING_PAGES
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = state.currentPage == pages.lastIndex

    LaunchedEffect(state.currentPage) {
        if (pagerState.currentPage != state.currentPage) {
            pagerState.animateScrollToPage(state.currentPage)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onEvent(OnboardingEvent.OnPageChanged(page))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = BACKGROUND_TINT_ALPHA),
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        SkipRow(visible = !isLastPage, onSkip = { onEvent(OnboardingEvent.OnSkipClicked) })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) { page ->
            OnboardingPageContent(
                page = pages[page],
                modifier = Modifier.pageTransition(pagerState, page),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.large)
                .padding(bottom = MaterialTheme.spacing.medium),
        ) {
            PageIndicator(totalPages = pages.size, selectedIndex = state.currentPage)
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            NextButton(isLastPage = isLastPage, onClick = { onEvent(OnboardingEvent.OnNextClicked) })
        }
    }
}

/** Fades and slightly scales pages as they are swiped away. */
private fun Modifier.pageTransition(pagerState: PagerState, page: Int): Modifier = graphicsLayer {
    val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
        .coerceIn(0f, 1f)
    alpha = 1f - offset * (1f - MIN_PAGE_ALPHA)
    val scale = 1f - offset * (1f - MIN_PAGE_SCALE)
    scaleX = scale
    scaleY = scale
}

@Composable
private fun SkipRow(visible: Boolean, onSkip: () -> Unit) {
    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.spacing.xxLarge)
            .padding(horizontal = MaterialTheme.spacing.small),
    ) {
        if (visible) {
            TextButton(onClick = onSkip) {
                Text(
                    text = stringResource(Res.string.onboarding_skip),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, modifier: Modifier = Modifier) {
    val illustrationDescription = stringResource(Res.string.onboarding_cd_illustration)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.large),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { contentDescription = illustrationDescription },
        ) {
            page.illustration(Modifier)
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        Text(
            text = stringResource(page.title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        Text(
            text = stringResource(page.subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier.fillMaxWidth(),
        ) {
            page.features.forEach { FeatureChip(stringResource(it)) }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
    }
}

@Composable
private fun FeatureChip(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FEATURE_ICON))
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.xSmall))
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PageIndicator(totalPages: Int, selectedIndex: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalPages) { index ->
            val isSelected = index == selectedIndex
            val width by animateDpAsState(if (isSelected) INDICATOR_ACTIVE_WIDTH else INDICATOR_DOT)
            val color by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            )
            Box(
                modifier = Modifier
                    .height(INDICATOR_DOT)
                    .width(width)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
private fun NextButton(isLastPage: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.spacing.buttonMinHeight),
    ) {
        AnimatedContent(
            targetState = isLastPage,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "nextButton",
        ) { last ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(if (last) Res.string.onboarding_start else Res.string.onboarding_next),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Icon(
                    imageVector = if (last) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
                )
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    DividoxTheme {
        OnboardingScreen(state = OnboardingState(), onEvent = {})
    }
}

@Preview
@Composable
private fun OnboardingScreenDarkPreview() {
    DividoxTheme(darkTheme = true) {
        OnboardingScreen(state = OnboardingState(currentPage = 4), onEvent = {})
    }
}
