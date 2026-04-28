package com.akole.dividox.common.ui.resources.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.ui_navigate_back
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DividoxTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    showDate: Boolean = false,
    onBack: (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = {
            if (showDate || onBack != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showDate) {
                        val dateText = remember {
                            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                            val month = today.month.name
                                .take(3)
                                .lowercase()
                                .replaceFirstChar { it.uppercase() }
                            "${today.day} $month"
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.ui_navigate_back),
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier,
    )
}
