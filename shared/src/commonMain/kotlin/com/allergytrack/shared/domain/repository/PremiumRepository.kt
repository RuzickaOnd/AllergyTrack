package com.allergytrack.shared.domain.repository

import com.allergytrack.shared.domain.model.PremiumStatus
import kotlinx.coroutines.flow.Flow

interface PremiumRepository {
    /** Reaktivní stream stavu předplatného. */
    fun observePremiumStatus(): Flow<PremiumStatus>

    /** Jednorázové synchronní načtení stavu. */
    suspend fun getPremiumStatus(): PremiumStatus

    /**
     * Spustí ověření nákupu přes Google Play Billing.
     * V MVP: no-op, vždy vrátí FREE.
     * V produkci: volá BillingClient.queryPurchasesAsync()
     */
    suspend fun refreshPremiumStatus()
}
