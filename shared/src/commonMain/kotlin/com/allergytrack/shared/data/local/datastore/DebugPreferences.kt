package com.allergytrack.shared.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


/**
 * Nastavení debug override pro testování různých stavů aplikace.
 * Persistuje přes restarty aplikace — ideální pro manuální QA testování.
 *
 * Používá se POUZE v BuildConfig.DEBUG buildu.
 * V release buildu jsou tyto hodnoty ignorovány (viz PremiumHelper).
 */
class DebugPreferences(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        const val FILE_NAME = "debug_preferences"

        private val KEY_PREMIUM_OVERRIDE = stringPreferencesKey("debug_premium_override")
        private val KEY_SYMPTOM_COUNT_OVERRIDE = intPreferencesKey("debug_symptom_count_override")
        private val KEY_ANALYTICS_ENABLED = booleanPreferencesKey("debug_analytics_enabled")
        private val KEY_CRASHLYTICS_ENABLED = booleanPreferencesKey("debug_crashlytics_enabled")
    }

    /** -1 = použít skutečná data, jinak override */
    val symptomCountOverride: Flow<Int> =
        dataStore.data.map { prefs ->
            prefs[KEY_SYMPTOM_COUNT_OVERRIDE] ?: -1
        }

    suspend fun setSymptomCountOverride(count: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_SYMPTOM_COUNT_OVERRIDE] = count
        }
    }

    /** Výslovné povolení Firebase Analytics v debug verzi */
    val analyticsEnabled: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[KEY_ANALYTICS_ENABLED] ?: false
        }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ANALYTICS_ENABLED] = enabled
        }
    }

    /** Výslovné povolení Firebase Crashlytics v debug verzi */
    val crashlyticsEnabled: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[KEY_CRASHLYTICS_ENABLED] ?: false
        }

    suspend fun setCrashlyticsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_CRASHLYTICS_ENABLED] = enabled
        }
    }

    /** Aktuální debug override stav prémiového předplatného */
    val premiumOverride: Flow<DebugPremiumOverride> =
        dataStore.data.map { prefs ->
            val raw = prefs[KEY_PREMIUM_OVERRIDE] ?: DebugPremiumOverride.USE_APP_STATE.name
            try {
                DebugPremiumOverride.valueOf(raw)
            } catch (e: IllegalArgumentException) {
                DebugPremiumOverride.USE_APP_STATE
            }
        }

    /** Uloží nový override stav */
    suspend fun setPremiumOverride(override: DebugPremiumOverride) {
        dataStore.edit { prefs ->
            prefs[KEY_PREMIUM_OVERRIDE] = override.name
        }
    }

    /** Resetuje všechna debug nastavení na výchozí hodnoty */
    suspend fun resetAll() {
        dataStore.edit { it.clear() }
    }
}

/**
 * Možné debug override stavy pro prémiové předplatné.
 */
enum class DebugPremiumOverride(val label: String, val description: String) {
    USE_APP_STATE(
        label = "Stav aplikace",
        description = "Respektuje visibilityMode nastavený v PremiumHelper.kt a skutečný stav předplatného"
    ),
    FORCE_VISIBLE_FREE(
        label = "VISIBLE + FREE",
        description = "Vynutí mód VISIBLE bez ohledu na visibilityMode — prémiové prvky jsou viditelné, zobrazují upsell karty a hvězdičky"
    ),
    FORCE_VISIBLE_PREMIUM(
        label = "VISIBLE + PREMIUM",
        description = "Vynutí mód VISIBLE bez ohledu na visibilityMode — všechny prémiové funkce jsou odemčené"
    )
}
