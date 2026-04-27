package com.akole.dividox.common.ui.resources.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val googleBlue = Color(0xFF4285F4)
private val googleRed = Color(0xFFEA4335)
private val googleYellow = Color(0xFFFBBC05)
private val googleGreen = Color(0xFF34A853)

private val googleLogoText: AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = googleBlue, fontWeight = FontWeight.Bold)) { append("G") }
    withStyle(SpanStyle(color = googleRed, fontWeight = FontWeight.Bold)) { append("o") }
    withStyle(SpanStyle(color = googleYellow, fontWeight = FontWeight.Bold)) { append("o") }
    withStyle(SpanStyle(color = googleBlue, fontWeight = FontWeight.Bold)) { append("g") }
    withStyle(SpanStyle(color = googleGreen, fontWeight = FontWeight.Bold)) { append("l") }
    withStyle(SpanStyle(color = googleRed, fontWeight = FontWeight.Bold)) { append("e") }
}

@Composable
fun SocialSignInButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = googleLogoText,
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}
