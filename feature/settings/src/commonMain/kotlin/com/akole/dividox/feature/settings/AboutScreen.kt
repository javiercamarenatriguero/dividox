package com.akole.dividox.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.about_description
import dividox.common.ui_resources.generated.resources.about_made_with
import dividox.common.ui_resources.generated.resources.about_tagline
import dividox.common.ui_resources.generated.resources.about_version_label
import dividox.common.ui_resources.generated.resources.settings_about
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutScreen(
    appVersion: String,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            DividoxTopAppBar(
                title = stringResource(Res.string.settings_about),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = MaterialTheme.spacing.medium),
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xLarge))
            Text(
                text = "DiviDox",
                style = MaterialTheme.typography.displaySmall,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xSmall))
            Text(
                text = stringResource(Res.string.about_tagline),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            Text(
                text = "${stringResource(Res.string.about_version_label)} $appVersion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            Text(
                text = stringResource(Res.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xxLarge))
            Text(
                text = stringResource(Res.string.about_made_with),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }
    }
}
