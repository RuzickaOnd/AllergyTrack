package com.allergytrack.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_FOOD_LANGUAGE = stringPreferencesKey("food_search_language")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_AVATAR_SELECTION = stringPreferencesKey("avatar_selection")
    }

    /** Uložený kód jazyka pro vyhledávání jídla. Prázdný string = auto (dle zařízení). */
    val foodSearchLanguage: Flow<String> = context.userPreferencesDataStore.data
        .map { prefs -> prefs[KEY_FOOD_LANGUAGE] ?: "" }

    suspend fun setFoodSearchLanguage(code: String) {
        context.userPreferencesDataStore.edit { it[KEY_FOOD_LANGUAGE] = code }
    }

    val userName: Flow<String> = context.userPreferencesDataStore.data
        .map { prefs -> prefs[KEY_USER_NAME] ?: "Anonym" }

    suspend fun setUserName(name: String) {
        context.userPreferencesDataStore.edit { it[KEY_USER_NAME] = name }
    }

    /** ID presetu (např. "person") nebo "gallery:<uri>" pro obrázek z galerie. */
    val avatarSelection: Flow<String> = context.userPreferencesDataStore.data
        .map { prefs -> prefs[KEY_AVATAR_SELECTION] ?: "person" }

    suspend fun setAvatarSelection(selection: String) {
        context.userPreferencesDataStore.edit { it[KEY_AVATAR_SELECTION] = selection }
    }
}
