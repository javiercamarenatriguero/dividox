package com.akole.dividox.common.ui.resources.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.ui_google_sign_in
import org.jetbrains.compose.resources.stringResource

@Composable
fun SocialSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth().heightIn(min = MaterialTheme.spacing.buttonMinHeight),
    ) {
        Icon(
            imageVector = GoogleLogoIcon,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
        Text(
            text = stringResource(Res.string.ui_google_sign_in),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
