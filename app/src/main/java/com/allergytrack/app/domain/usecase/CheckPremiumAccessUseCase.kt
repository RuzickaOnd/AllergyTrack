package com.allergytrack.app.domain.usecase

import com.allergytrack.shared.domain.model.PremiumFeature
import com.allergytrack.app.domain.premium.PremiumHelper
import kotlinx.coroutines.flow.Flow

class CheckPremiumAccessUseCase(
    private val premiumHelper: PremiumHelper
) {
    operator fun invoke(feature: PremiumFeature): Flow<Boolean> =
        premiumHelper.observeAccess(feature)
}
