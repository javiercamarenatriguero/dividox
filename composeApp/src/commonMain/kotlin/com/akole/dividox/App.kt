package com.akole.dividox

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.akole.dividox.navigation.SetupRootNavGraph

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        SetupRootNavGraph(navController)
    }
}
