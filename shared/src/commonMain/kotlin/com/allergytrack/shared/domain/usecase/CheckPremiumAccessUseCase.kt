package com.allergytrack.shared.domain.usecase

import com.allergytrack.shared.domain.model.PremiumFeature
import com.allergytrack.shared.domain.premium.PremiumHelper
import kotlinx.coroutines.flow.Flow

class CheckPremiumAccessUseCase(
    private val premiumHelper: PremiumHelper
) {
    operator fun invoke(feature: PremiumFeature): Flow<Boolean> =
        premiumHelper.observeAccess(feature)
}
