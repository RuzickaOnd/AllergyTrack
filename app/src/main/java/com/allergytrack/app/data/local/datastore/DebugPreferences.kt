package com.allergytrack.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.debugDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "debug_preferences")

/**
 * Nastavení debug override pro testování různých stavů aplikace.
 * Persistuje přes restarty aplikace — ideální pro manuální QA testování.
 *
 * Používá se POUZE v BuildConfig.DEBUG buildu.
 * V release buildu jsou tyto hodnoty ignorovány (viz PremiumHelper).
 */
@Singleton
class DebugPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_PREMIUM_OVERRIDE = stringPreferencesKey("debug_premium_override")
        private val KEY_SYMPTOM_COUNT_OVERRIDE = intPreferencesKey("debug_symptom_count_override")
    }

    /** -1 = použít skutečná data, jinak override */
    val symptomCountOverride: Flow<Int> =
        context.debugDataStore.data.map { prefs ->
            prefs[KEY_SYMPTOM_COUNT_OVERRIDE] ?: -1
        }

    suspend fun setSymptomCountOverride(count: Int) {
        context.debugDataStore.edit { prefs ->
            prefs[KEY_SYMPTOM_COUNT_OVERRIDE] = count
        }
    }

    /** Aktuální debug override stav prémiového předplatného */
    val premiumOverride: Flow<DebugPremiumOverride> =
        context.debugDataStore.data.map { prefs ->
            val raw = prefs[KEY_PREMIUM_OVERRIDE] ?: DebugPremiumOverride.USE_APP_STATE.name
            try {
                DebugPremiumOverride.valueOf(raw)
            } catch (e: IllegalArgumentException) {
                DebugPremiumOverride.USE_APP_STATE
            }
        }

    /** Uloží nový override stav */
    suspend fun setPremiumOverride(override: DebugPremiumOverride) {
        context.debugDataStore.edit { prefs ->
            prefs[KEY_PREMIUM_OVERRIDE] = override.name
        }
    }

    /** Resetuje všechna debug nastavení na výchozí hodnoty */
    suspend fun resetAll() {
        context.debugDataStore.edit { it.clear() }
    }
}

/**
 * Možné debug override stavy pro prémiové předplatné.
 */
enum class DebugPremiumOverride(val label: String, val description: String) {
    USE_APP_STATE(
        label = "Stav aplikace",
        description = "Řídí se skutečným stavem PremiumHelper.visibilityMode"
    ),
    FORCE_FREE(
        label = "Vynuceno FREE",
        description = "Simuluje nepřihlášeného / free uživatele bez ohledu na nastavení"
    ),
    FORCE_PREMIUM(
        label = "Vynuceno PREMIUM",
        description = "Simuluje platícího uživatele, všechny prémiové funkce odemčeny"
    ),
    FORCE_VISIBLE_FREE(
        label = "VISIBLE + FREE",
        description = "Zobrazí všechny prémiové prvky s hvězdičkou a upsell kartami"
    ),
    FORCE_VISIBLE_PREMIUM(
        label = "VISIBLE + PREMIUM",
        description = "Zobrazí všechny prémiové prvky jako odemčené (platící uživatel)"
    )
}
