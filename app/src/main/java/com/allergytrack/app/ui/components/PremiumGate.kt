package com.allergytrack.app.ui.components

import androidx.compose.runtime.Composable
import com.allergytrack.shared.domain.model.PremiumFeature

/**
 * Třístavový wrapper:
 *   isVisible = false              → nezobrazí NIC
 *   isVisible = true, showUpsell   → zobrazí PremiumUpsellCard
 *   isVisible = true, !showUpsell  → zobrazí content
 */
@Composable
fun PremiumGate(
    isVisible: Boolean,
    showUpsell: Boolean,
    feature: PremiumFeature,
    onUpgradeClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    when {
        !isVisible -> { /* HIDDEN: nezobrazit nic */ }
        showUpsell -> PremiumUpsellCard(feature = feature, onUpgradeClick = onUpgradeClick)
        else       -> content()
    }
}
