package com.allergytrack.shared.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class OnboardingDataStore(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        const val FILE_NAME = "onboarding_preferences"

        private val KEY_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_ALLERGY_TYPES = stringPreferencesKey("selected_allergy_types")
        private val KEY_SYMPTOM_TYPES = stringPreferencesKey("selected_symptom_types")
    }

    /** Je onboarding dokončen? */
    val isCompleted: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[KEY_COMPLETED] ?: false
        }

    /** Vybrané typy alergií (serializované jako JSON string) */
    val selectedAllergyTypes: Flow<String> =
        dataStore.data.map { prefs ->
            prefs[KEY_ALLERGY_TYPES] ?: ""
        }

    /** Vybrané typy příznaků z onboardingu */
    val selectedSymptomTypes: Flow<String> =
        dataStore.data.map { prefs ->
            prefs[KEY_SYMPTOM_TYPES] ?: ""
        }

    suspend fun setCompleted() {
        dataStore.edit { prefs ->
            prefs[KEY_COMPLETED] = true
        }
    }

    suspend fun saveAllergyProfile(allergyTypes: Set<String>, symptomTypes: Set<String>) {
        dataStore.edit { prefs ->
            prefs[KEY_ALLERGY_TYPES] = allergyTypes.joinToString(",")
            prefs[KEY_SYMPTOM_TYPES] = symptomTypes.joinToString(",")
        }
    }

    /** Pro debug/reset — smaže onboarding stav */
    suspend fun reset() {
        dataStore.edit { it.clear() }
    }
}
