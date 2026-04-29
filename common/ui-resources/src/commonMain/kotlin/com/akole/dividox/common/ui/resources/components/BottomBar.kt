package com.akole.dividox.common.ui.resources.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.section_dashboard
import dividox.common.ui_resources.generated.resources.section_dividends
import dividox.common.ui_resources.generated.resources.section_portfolio
import dividox.common.ui_resources.generated.resources.section_settings
import org.jetbrains.compose.resources.stringResource

enum class BottomTab { DASHBOARD, PORTFOLIO, DIVIDENDS, SETTINGS }

@Composable
fun DividoxBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = selectedTab == BottomTab.DASHBOARD,
            onClick = { onTabSelected(BottomTab.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomTab.DASHBOARD) {
                        Icons.Filled.Dashboard
                    } else {
                        Icons.Outlined.Dashboard
                    },
                    contentDescription = stringResource(Res.string.section_dashboard),
                )
            },
            label = { Text(stringResource(Res.string.section_dashboard)) },
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.PORTFOLIO,
            onClick = { onTabSelected(BottomTab.PORTFOLIO) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomTab.PORTFOLIO) {
                        Icons.Filled.Wallet
                    } else {
                        Icons.Outlined.Wallet
                    },
                    contentDescription = stringResource(Res.string.section_portfolio),
                )
            },
            label = { Text(stringResource(Res.string.section_portfolio)) },
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.DIVIDENDS,
            onClick = { onTabSelected(BottomTab.DIVIDENDS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomTab.DIVIDENDS) {
                        Icons.Filled.BarChart
                    } else {
                        Icons.Outlined.BarChart
                    },
                    contentDescription = stringResource(Res.string.section_dividends),
                )
            },
            label = { Text(stringResource(Res.string.section_dividends)) },
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.SETTINGS,
            onClick = { onTabSelected(BottomTab.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomTab.SETTINGS) {
                        Icons.Filled.Settings
                    } else {
                        Icons.Outlined.Settings
                    },
                    contentDescription = stringResource(Res.string.section_settings),
                )
            },
            label = { Text(stringResource(Res.string.section_settings)) },
        )
    }
}
