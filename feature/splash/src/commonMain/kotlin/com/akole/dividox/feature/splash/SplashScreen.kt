package com.akole.dividox.feature.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.ic_launcher_fore
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

private const val ICON_VISIBLE_DELAY_MS = 400L
private const val TEXT_ANIMATION_DURATION_MS = 450

@Composable
fun SplashScreen() {
    var showText by remember { mutableStateOf(false) }

    val textAlpha by animateFloatAsState(
        targetValue = if (showText) 1f else 0f,
        animationSpec = tween(durationMillis = TEXT_ANIMATION_DURATION_MS),
        label = "textAlpha",
    )
    val textTranslationX by animateFloatAsState(
        targetValue = if (showText) 0f else -300f,
        animationSpec = tween(durationMillis = TEXT_ANIMATION_DURATION_MS),
        label = "textTranslationX",
    )

    LaunchedEffect(Unit) {
        delay(ICON_VISIBLE_DELAY_MS)
        showText = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.ic_launcher_fore),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
            )
            Text(
                text = "DiviDox",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .graphicsLayer {
                        alpha = textAlpha
                        translationX = textTranslationX
                    },
            )
        }
    }
}
