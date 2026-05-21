package com.akole.dividox.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.onboarding_1
import dividox.common.ui_resources.generated.resources.onboarding_2
import dividox.common.ui_resources.generated.resources.onboarding_3
import dividox.common.ui_resources.generated.resources.onboarding_4
import dividox.common.ui_resources.generated.resources.onboarding_5
import dividox.common.ui_resources.generated.resources.onboarding_next
import dividox.common.ui_resources.generated.resources.onboarding_page1_subtitle
import dividox.common.ui_resources.generated.resources.onboarding_page1_title
import dividox.common.ui_resources.generated.resources.onboarding_page2_subtitle
import dividox.common.ui_resources.generated.resources.onboarding_page2_title
import dividox.common.ui_resources.generated.resources.onboarding_page3_subtitle
import dividox.common.ui_resources.generated.resources.onboarding_page3_title
import dividox.common.ui_resources.generated.resources.onboarding_page4_subtitle
import dividox.common.ui_resources.generated.resources.onboarding_page4_title
import dividox.common.ui_resources.generated.resources.onboarding_page5_subtitle
import dividox.common.ui_resources.generated.resources.onboarding_page5_title
import dividox.common.ui_resources.generated.resources.onboarding_skip
import dividox.common.ui_resources.generated.resources.onboarding_start
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private data class OnboardingPage(
    val image: DrawableResource,
    val title: String,
    val subtitle: String,
)

@Composable
private fun onboardingPages() = listOf(
    OnboardingPage(Res.drawable.onboarding_1, stringResource(Res.string.onboarding_page1_title), stringResource(Res.string.onboarding_page1_subtitle)),
    OnboardingPage(Res.drawable.onboarding_2, stringResource(Res.string.onboarding_page2_title), stringResource(Res.string.onboarding_page2_subtitle)),
    OnboardingPage(Res.drawable.onboarding_3, stringResource(Res.string.onboarding_page3_title), stringResource(Res.string.onboarding_page3_subtitle)),
    OnboardingPage(Res.drawable.onboarding_4, stringResource(Res.string.onboarding_page4_title), stringResource(Res.string.onboarding_page4_subtitle)),
    OnboardingPage(Res.drawable.onboarding_5, stringResource(Res.string.onboarding_page5_title), stringResource(Res.string.onboarding_page5_subtitle)),
)

private val CARD_CORNER = 32.dp

@Composable
fun OnboardingScreen(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = onboardingPages()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = state.currentPage == pages.size - 1

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

    Box(modifier = modifier.fillMaxSize()) {

        // ── Image area (top 55%) ─────────────────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.57f),
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(pages[page].image),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .padding(horizontal = MaterialTheme.spacing.medium),
                )
            }
        }

        // ── Bottom card ──────────────────────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = CARD_CORNER, topEnd = CARD_CORNER),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = MaterialTheme.spacing.large)
                    .padding(top = MaterialTheme.spacing.large, bottom = MaterialTheme.spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DotsIndicator(
                    totalDots = pages.size,
                    selectedIndex = state.currentPage,
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                Text(
                    text = pages[state.currentPage].title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                Text(
                    text = pages[state.currentPage].subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xLarge))

                Button(
                    onClick = { onEvent(OnboardingEvent.OnNextClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MaterialTheme.spacing.buttonMinHeight),
                ) {
                    Text(
                        text = if (isLastPage) stringResource(Res.string.onboarding_start)
                        else stringResource(Res.string.onboarding_next),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                if (!isLastPage) {
                    TextButton(
                        onClick = { onEvent(OnboardingEvent.OnSkipClicked) },
                        modifier = Modifier.padding(top = MaterialTheme.spacing.xSmall),
                    ) {
                        Text(
                            text = stringResource(Res.string.onboarding_skip),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                }
            }
        }

        // ── Page counter (overlay, top-right) ────────────────────────────────
        Text(
            text = "${state.currentPage + 1}/${state.totalPages}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(
                    end = MaterialTheme.spacing.medium,
                    top = MaterialTheme.spacing.small,
                ),
        )
    }
}

@Composable
private fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalDots) { index ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(if (isSelected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ),
            )
        }
    }
}
