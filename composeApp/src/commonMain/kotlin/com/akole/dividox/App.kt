package com.akole.dividox

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.akole.dividox.common.ui.resources.theme.DividoxTheme
import com.akole.dividox.navigation.SetupRootNavGraph

@Composable
@Preview
fun App() {
    DividoxTheme {
        val navController = rememberNavController()
        SetupRootNavGraph(navController)
    }
}
