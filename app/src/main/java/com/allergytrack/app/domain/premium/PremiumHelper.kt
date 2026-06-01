package com.allergytrack.app.domain.premium

import com.allergytrack.app.BuildConfig
import com.allergytrack.app.data.local.datastore.DebugPremiumOverride
import com.allergytrack.app.data.local.datastore.DebugPreferences
import com.allergytrack.app.domain.model.PremiumFeature
import com.allergytrack.app.domain.model.PremiumStatus
import com.allergytrack.app.domain.model.PremiumVisibilityMode
import com.allergytrack.app.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumHelper @Inject constructor(
    private val premiumRepository: PremiumRepository,
    private val debugPreferences: DebugPreferences
) {
    // =========================================================
    // HLAVNÍ PŘEPÍNAČ — mění se POUZE zde
    // HIDDEN  = funkce fyzicky neexistují v UI
    // VISIBLE = funkce viditelné, free uživatel vidí hvězdičky
    // =========================================================
    private val visibilityMode = PremiumVisibilityMode.VISIBLE

    val currentVisibilityMode: PremiumVisibilityMode get() = visibilityMode

    private val premiumFeatures: Set<PremiumFeature> = setOf(
        PremiumFeature.FOOD_PHOTO_RECOGNITION,
        PremiumFeature.POLLEN_DATA,
        PremiumFeature.EXTENDED_CHARTS,
        PremiumFeature.AI_CORRELATION_ANALYSIS,
        PremiumFeature.PDF_EXPORT,
        PremiumFeature.UNLIMITED_HISTORY
    )

    // ---- Resolved state (s debug override) ----

    /**
     * Efektivní visibility mode — v DEBUG může být přepsán z DataStore.
     * V RELEASE vždy vrátí produkční visibilityMode.
     */
    private fun observeEffectiveVisibility(): Flow<PremiumVisibilityMode> {
        if (!BuildConfig.DEBUG) return flowOf(visibilityMode)
        return debugPreferences.premiumOverride.map { override ->
            when (override) {
                DebugPremiumOverride.USE_APP_STATE -> visibilityMode
                DebugPremiumOverride.FORCE_VISIBLE_FREE -> PremiumVisibilityMode.VISIBLE
                DebugPremiumOverride.FORCE_VISIBLE_PREMIUM -> PremiumVisibilityMode.VISIBLE
            }
        }
    }

    /**
     * Efektivní premium status — v DEBUG může být přepsán z DataStore.
     * V RELEASE vždy vrátí skutečný stav z PremiumRepository.
     */
    fun observeEffectiveStatus(): Flow<PremiumStatus> {
        if (!BuildConfig.DEBUG) return premiumRepository.observePremiumStatus()
        return combine(
            premiumRepository.observePremiumStatus(),
            debugPreferences.premiumOverride
        ) { realStatus, override ->
            when (override) {
                DebugPremiumOverride.USE_APP_STATE -> realStatus
                DebugPremiumOverride.FORCE_VISIBLE_FREE -> PremiumStatus.FREE
                DebugPremiumOverride.FORCE_VISIBLE_PREMIUM -> PremiumStatus.PREMIUM
            }
        }
    }

    // ---- Public API (stejné jako dříve, teď čtou effective hodnoty) ----

    fun observeVisible(feature: PremiumFeature): Flow<Boolean> =
        observeEffectiveVisibility().map { mode ->
            mode == PremiumVisibilityMode.VISIBLE
        }

    fun observeAccess(feature: PremiumFeature): Flow<Boolean> =
        combine(observeEffectiveVisibility(), observeEffectiveStatus()) { mode, status ->
            resolveAccess(feature, mode, status)
        }

    fun observeShowBadge(feature: PremiumFeature): Flow<Boolean> =
        combine(observeEffectiveVisibility(), observeEffectiveStatus()) { mode, status ->
            mode == PremiumVisibilityMode.VISIBLE
                && feature in premiumFeatures
                && status == PremiumStatus.FREE
        }

    fun observeShowUpsell(feature: PremiumFeature): Flow<Boolean> =
        combine(observeEffectiveVisibility(), observeEffectiveStatus()) { mode, status ->
            mode == PremiumVisibilityMode.VISIBLE
                && feature in premiumFeatures
                && status == PremiumStatus.FREE
        }

    suspend fun hasAccess(feature: PremiumFeature): Boolean {
        val mode = if (BuildConfig.DEBUG) {
            observeEffectiveVisibility().first()
        } else {
            visibilityMode
        }

        val status = if (BuildConfig.DEBUG) {
            observeEffectiveStatus().first()
        } else {
            premiumRepository.getPremiumStatus()
        }

        return resolveAccess(feature, mode, status)
    }

    suspend fun isVisible(feature: PremiumFeature): Boolean {
        val mode = if (BuildConfig.DEBUG) {
            observeEffectiveVisibility().first()
        } else {
            visibilityMode
        }
        return mode == PremiumVisibilityMode.VISIBLE
    }

    suspend fun refresh() = premiumRepository.refreshPremiumStatus()

    private fun resolveAccess(
        feature: PremiumFeature,
        mode: PremiumVisibilityMode,
        status: PremiumStatus
    ): Boolean = when (mode) {
        PremiumVisibilityMode.HIDDEN -> false
        PremiumVisibilityMode.VISIBLE ->
            feature !in premiumFeatures || status != PremiumStatus.FREE
    }
}
