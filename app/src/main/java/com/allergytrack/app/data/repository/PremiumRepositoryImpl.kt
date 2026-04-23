package com.allergytrack.app.data.repository

import com.allergytrack.app.domain.model.PremiumStatus
import com.allergytrack.app.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MVP implementace — vždy vrací FREE.
 *
 * TODO (Fáze monetizace):
 *  1. Přidat závislost na Google Play Billing Library 7.x
 *  2. Inicializovat BillingClient v init bloku
 *  3. queryPurchasesAsync() v refreshPremiumStatus()
 *  4. Uložit výsledek do DataStore
 *  5. observePremiumStatus() číst z DataStore
 */
@Singleton
class PremiumRepositoryImpl @Inject constructor() : PremiumRepository {

    override fun observePremiumStatus(): Flow<PremiumStatus> =
        flowOf(PremiumStatus.FREE)

    override suspend fun getPremiumStatus(): PremiumStatus =
        PremiumStatus.FREE

    override suspend fun refreshPremiumStatus() {
        // MVP: no-op
        // Produkce: BillingClient.queryPurchasesAsync()
    }
}
