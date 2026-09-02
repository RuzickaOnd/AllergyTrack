package com.allergytrack.app.data.repository

import com.allergytrack.shared.domain.model.PremiumStatus
import com.allergytrack.shared.domain.repository.PremiumRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PeriodType
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class PremiumRepositoryImpl : PremiumRepository {

    private val ENTITLEMENT_ID = "AllergyTrack Premium"

    private val _premiumStatus = MutableStateFlow(PremiumStatus.FREE)

    init {
        // Nastavení listeneru na aktualizace zákaznických dat (nákupy, restory atd.)
        Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
            _premiumStatus.value = mapCustomerInfoToPremiumStatus(customerInfo)
        }

        // Získání počátečních dat (čte se nejprve z cache, následně se asynchronně ověřuje se serverem)
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _premiumStatus.value = mapCustomerInfoToPremiumStatus(customerInfo)
            }

            override fun onError(error: PurchasesError) {
                // V případě chyby (např. Offline) zůstává předchozí (případně free) stav
            }
        })
    }

    override fun observePremiumStatus(): Flow<PremiumStatus> = _premiumStatus.asStateFlow()

    override suspend fun getPremiumStatus(): PremiumStatus {
        return suspendCancellableCoroutine { continuation ->
            Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    val status = mapCustomerInfoToPremiumStatus(customerInfo)
                    _premiumStatus.value = status
                    if (continuation.isActive) continuation.resume(status)
                }

                override fun onError(error: PurchasesError) {
                    // V případě chyby vrátíme aktuální lokální stav
                    if (continuation.isActive) continuation.resume(_premiumStatus.value)
                }
            })
        }
    }

    override suspend fun refreshPremiumStatus() {
        suspendCancellableCoroutine<Unit> { continuation ->
            // Vyčištění cache a vynucení stažení nejnovějších dat ze serveru
            Purchases.sharedInstance.invalidateCustomerInfoCache()
            Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    _premiumStatus.value = mapCustomerInfoToPremiumStatus(customerInfo)
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onError(error: PurchasesError) {
                    if (continuation.isActive) continuation.resume(Unit)
                }
            })
        }
    }

    /**
     * Mapuje CustomerInfo z RevenueCat na náš interní enum PremiumStatus.
     */
    private fun mapCustomerInfoToPremiumStatus(customerInfo: CustomerInfo): PremiumStatus {
        val entitlement = customerInfo.entitlements[ENTITLEMENT_ID]
        return if (entitlement?.isActive == true) {
            when (entitlement.periodType) {
                PeriodType.TRIAL -> PremiumStatus.TRIAL
                else -> PremiumStatus.PREMIUM
            }
        } else {
            PremiumStatus.FREE
        }
    }
}
