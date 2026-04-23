package com.allergytrack.app.domain.usecase

import com.allergytrack.app.domain.model.PremiumFeature
import com.allergytrack.app.domain.premium.PremiumHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckPremiumAccessUseCase @Inject constructor(
    private val premiumHelper: PremiumHelper
) {
    operator fun invoke(feature: PremiumFeature): Flow<Boolean> =
        premiumHelper.observeAccess(feature)
}
