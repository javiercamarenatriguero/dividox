package com.akole.dividox.common.ui.resources.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.ui.resources.format.formatPercent
import com.akole.dividox.common.ui.resources.format.formatPercentSigned
import com.akole.dividox.common.ui.resources.format.formatPrice
import com.akole.dividox.common.ui.resources.theme.extendedColors
import com.akole.dividox.common.ui.resources.theme.spacing
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.cd_add_to_favourites
import dividox.common.ui_resources.generated.resources.cd_remove_from_favourites
import dividox.common.ui_resources.generated.resources.label_in_portfolio
import org.jetbrains.compose.resources.stringResource

@Composable
fun SecurityCard(
    ticker: String,
    companyName: String?,
    price: Double?,
    changePercent: Double?,
    currency: Currency,
    isFavorite: Boolean,
    isInPortfolio: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    securityType: String? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
                ) {
                    Text(
                        text = ticker,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (securityType != null) {
                        Text(
                            text = securityType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (companyName != null) {
                    Text(
                        text = companyName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isInPortfolio) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(Res.string.label_in_portfolio),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            if (price != null) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small),
                ) {
                    AnimatedValueText(
                        value = price.formatPrice(currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    if (changePercent != null) {
                        Text(
                            text = changePercent.formatPercentSigned(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (changePercent >= 0) MaterialTheme.extendedColors.profit
                            else MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(MaterialTheme.spacing.iconMedium + 8.dp),
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isFavorite) Res.string.cd_remove_from_favourites else Res.string.cd_add_to_favourites
                    ),
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
                )
            }
        }
    }
}
