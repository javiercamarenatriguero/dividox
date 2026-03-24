package com.akole.dividox.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.feature.home.HomeContract.HomeSideEffect
import com.akole.dividox.feature.home.HomeContract.HomeViewEvent
import com.akole.dividox.feature.home.HomeContract.HomeViewState
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.compose_multiplatform
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeScreen(
    state: HomeViewState,
    onEvent: (HomeViewEvent) -> Unit,
    sideEffects: Flow<HomeSideEffect>,
    onNavigation: (HomeSideEffect.Navigation) -> Unit,
) {
    CollectSideEffect(sideEffects) { effect ->
        when (effect) {
            is HomeSideEffect.Navigation -> onNavigation(effect)
        }
    }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = { onEvent(HomeViewEvent.OnButtonClicked) }) {
            Text("Click me!")
        }
        AnimatedVisibility(state.showContent) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Compose: ${state.greeting}")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onEvent(HomeViewEvent.OnDetailClicked) }) {
            Text("View Platform Details")
        }
    }
}
