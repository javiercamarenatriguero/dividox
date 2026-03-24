package com.akole.dividox

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akole.dividox.common.mvi.collectViewState

@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel: AppViewModel = viewModel { AppViewModel() }
        val state by collectViewState(viewModel.viewState)

        AppScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
        )
    }
}
