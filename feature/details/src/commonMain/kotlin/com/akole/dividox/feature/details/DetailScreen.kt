package com.akole.dividox.feature.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.akole.dividox.feature.details.DetailContract.DetailSideEffect
import com.akole.dividox.feature.details.DetailContract.DetailViewEvent
import com.akole.dividox.feature.details.DetailContract.DetailViewState
import kotlinx.coroutines.flow.Flow

@Composable
fun DetailScreen(
    state: DetailViewState,
    onEvent: (DetailViewEvent) -> Unit,
    sideEffects: Flow<DetailSideEffect>,
    onNavigation: (DetailSideEffect.Navigation) -> Unit,
) {
    CollectSideEffect(sideEffects) { effect ->
        when (effect) {
            is DetailSideEffect.Navigation -> onNavigation(effect)
        }
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Platform Details",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Platform: ${state.platformName}",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.greeting,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onEvent(DetailViewEvent.OnBackClicked) }) {
            Text("Go Back")
        }
    }
}
