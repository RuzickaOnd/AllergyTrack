package com.allergytrack.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allergytrack.app.data.analytics.AnalyticsManager
import com.allergytrack.shared.domain.model.PremiumStatus
import com.allergytrack.shared.domain.premium.PremiumHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PremiumViewModel(
    private val premiumHelper: PremiumHelper,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    val premiumStatus: StateFlow<PremiumStatus> = premiumHelper
        .observeEffectiveStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PremiumStatus.FREE)

    fun logPurchaseStarted(packageName: String) {
        analyticsManager.logPremiumInteraction("paywall", "purchase_started_$packageName")
    }

    fun logPurchaseCompleted() {
        analyticsManager.logPremiumInteraction("paywall", "purchase_success")
        viewModelScope.launch {
            premiumHelper.refresh()
        }
    }

    fun logPurchaseError(errorCode: String, errorMessage: String) {
        analyticsManager.logPremiumInteraction("paywall", "purchase_error_$errorCode: $errorMessage")
    }

    fun logPurchaseCancelled() {
        analyticsManager.logPremiumInteraction("paywall", "purchase_cancelled")
    }

    fun logRestoreCompleted() {
        analyticsManager.logPremiumInteraction("paywall", "restore_success")
        viewModelScope.launch {
            premiumHelper.refresh()
        }
    }
}
