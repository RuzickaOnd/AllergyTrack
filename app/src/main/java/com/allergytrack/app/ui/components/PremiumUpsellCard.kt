package com.allergytrack.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allergytrack.app.R
import com.allergytrack.app.domain.model.PremiumFeature
import com.allergytrack.app.ui.theme.*
import com.allergytrack.app.ui.util.displayNameRes

/**
 * Zobrazí se POUZE v VISIBLE módu pro FREE uživatele.
 * V HIDDEN módu se nikdy nevykreslí (PremiumGate to zaručuje).
 */
@Composable
fun PremiumUpsellCard(
    feature: PremiumFeature,
    modifier: Modifier = Modifier,
    onUpgradeClick: () -> Unit = {}
) {
    val borderColor = MaterialTheme.colorScheme.warningColor.copy(alpha = 0.5f)
    AppCard(
        modifier = modifier,
        borderColor = borderColor,
        backgroundColor = MaterialTheme.colorScheme.warningColor.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Lock, null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.warningColor
                )
                Text(
                    stringResource(feature.displayNameRes()),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                stringResource(R.string.premium_upsell_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.warningColor,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Text(
                    stringResource(R.string.btn_upgrade_premium).uppercase(),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Malý badge ⭐ Premium zobrazený vedle prvku.
 * Zobrazí se POUZE v VISIBLE módu pro FREE uživatele.
 */
@Composable
fun PremiumBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                Icons.Default.Star, null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                stringResource(R.string.premium_badge_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                fontFamily = MonoFontFamily
            )
        }
    }
}
