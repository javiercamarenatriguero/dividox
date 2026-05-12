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
import dividox.common.ui_resources.generated.resources.settings_terms
import dividox.common.ui_resources.generated.resources.terms_body_accuracy
import dividox.common.ui_resources.generated.resources.terms_body_changes
import dividox.common.ui_resources.generated.resources.terms_body_informational
import dividox.common.ui_resources.generated.resources.terms_body_liability
import dividox.common.ui_resources.generated.resources.terms_section_accuracy
import dividox.common.ui_resources.generated.resources.terms_section_changes
import dividox.common.ui_resources.generated.resources.terms_section_informational
import dividox.common.ui_resources.generated.resources.terms_section_liability
import org.jetbrains.compose.resources.stringResource

@Composable
fun TermsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            DividoxTopAppBar(
                title = stringResource(Res.string.settings_terms),
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
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            LegalSection(
                title = stringResource(Res.string.terms_section_informational),
                body = stringResource(Res.string.terms_body_informational),
            )
            LegalSection(
                title = stringResource(Res.string.terms_section_accuracy),
                body = stringResource(Res.string.terms_body_accuracy),
            )
            LegalSection(
                title = stringResource(Res.string.terms_section_liability),
                body = stringResource(Res.string.terms_body_liability),
            )
            LegalSection(
                title = stringResource(Res.string.terms_section_changes),
                body = stringResource(Res.string.terms_body_changes),
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }
    }
}

@Composable
internal fun LegalSection(title: String, body: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
}
