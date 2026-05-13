package com.akole.dividox.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.akole.dividox.common.ui.resources.components.DividoxTopAppBar
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.privacy_body_contact
import dividox.common.ui_resources.generated.resources.privacy_body_data
import dividox.common.ui_resources.generated.resources.privacy_body_security
import dividox.common.ui_resources.generated.resources.privacy_body_third_party
import dividox.common.ui_resources.generated.resources.privacy_section_contact
import dividox.common.ui_resources.generated.resources.privacy_section_data
import dividox.common.ui_resources.generated.resources.privacy_section_security
import dividox.common.ui_resources.generated.resources.privacy_section_third_party
import dividox.common.ui_resources.generated.resources.settings_privacy
import org.jetbrains.compose.resources.stringResource

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            DividoxTopAppBar(
                title = stringResource(Res.string.settings_privacy),
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
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            LegalCard {
                LegalSection(
                    title = stringResource(Res.string.privacy_section_data),
                    body = stringResource(Res.string.privacy_body_data),
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
                LegalSection(
                    title = stringResource(Res.string.privacy_section_third_party),
                    body = stringResource(Res.string.privacy_body_third_party),
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
                LegalSection(
                    title = stringResource(Res.string.privacy_section_security),
                    body = stringResource(Res.string.privacy_body_security),
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
                LegalSection(
                    title = stringResource(Res.string.privacy_section_contact),
                    body = stringResource(Res.string.privacy_body_contact),
                )
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }
    }
}
