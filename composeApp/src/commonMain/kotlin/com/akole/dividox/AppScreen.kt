package com.akole.dividox

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
import com.akole.dividox.AppContract.AppViewEvent
import com.akole.dividox.AppContract.AppViewState
import dividox.composeapp.generated.resources.Res
import dividox.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppScreen(
    state: AppViewState,
    onEvent: (AppViewEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = { onEvent(AppViewEvent.OnButtonClicked) }) {
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
        Button(onClick = { onEvent(AppViewEvent.OnDetailClicked) }) {
            Text("View Platform Details")
        }
    }
}
