package com.allergytrack.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "onboarding_preferences")

@Singleton
class OnboardingDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_ALLERGY_TYPES = stringPreferencesKey("selected_allergy_types")
        private val KEY_SYMPTOM_TYPES = stringPreferencesKey("selected_symptom_types")
    }

    /** Je onboarding dokončen? */
    val isCompleted: Flow<Boolean> =
        context.onboardingDataStore.data.map { prefs ->
            prefs[KEY_COMPLETED] ?: false
        }

    /** Vybrané typy alergií (serializované jako JSON string) */
    val selectedAllergyTypes: Flow<String> =
        context.onboardingDataStore.data.map { prefs ->
            prefs[KEY_ALLERGY_TYPES] ?: ""
        }

    suspend fun setCompleted() {
        context.onboardingDataStore.edit { prefs ->
            prefs[KEY_COMPLETED] = true
        }
    }

    suspend fun saveAllergyProfile(allergyTypes: Set<String>, symptomTypes: Set<String>) {
        context.onboardingDataStore.edit { prefs ->
            prefs[KEY_ALLERGY_TYPES] = allergyTypes.joinToString(",")
            prefs[KEY_SYMPTOM_TYPES] = symptomTypes.joinToString(",")
        }
    }

    /** Pro debug/reset — smaže onboarding stav */
    suspend fun reset() {
        context.onboardingDataStore.edit { it.clear() }
    }
}
