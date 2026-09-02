package com.allergytrack.shared.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class UserPreferencesDataStore(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        const val FILE_NAME = "user_preferences"

        private val KEY_FOOD_LANGUAGE = stringPreferencesKey("food_search_language")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_AVATAR_SELECTION = stringPreferencesKey("avatar_selection")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_SYNC_CHARTS_SCROLL = booleanPreferencesKey("sync_charts_scroll")
        private val KEY_NOTIFICATION_BANNER_DISMISSED = booleanPreferencesKey("notification_banner_dismissed")
    }

    /** Uložený kód jazyka pro vyhledávání jídla. Prázdný string = auto (dle zařízení). */
    val foodSearchLanguage: Flow<String> = dataStore.data
        .map { prefs -> prefs[KEY_FOOD_LANGUAGE] ?: "" }

    suspend fun setFoodSearchLanguage(code: String) {
        dataStore.edit { it[KEY_FOOD_LANGUAGE] = code }
    }

    val userName: Flow<String> = dataStore.data
        .map { prefs -> prefs[KEY_USER_NAME] ?: "Anonym" }

    suspend fun setUserName(name: String) {
        dataStore.edit { it[KEY_USER_NAME] = name }
    }

    /** ID presetu (např. "person") nebo "gallery:<uri>" pro obrázek z galerie. */
    val avatarSelection: Flow<String> = dataStore.data
        .map { prefs -> prefs[KEY_AVATAR_SELECTION] ?: "person" }

    suspend fun setAvatarSelection(selection: String) {
        dataStore.edit { it[KEY_AVATAR_SELECTION] = selection }
    }

    /** Tmavý režim. Výchozí = true (dark-first design). */
    val darkMode: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[KEY_DARK_MODE] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    /** Push notifikace — večerní připomínka. Výchozí = false. */
    val notificationsEnabled: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[KEY_NOTIFICATIONS_ENABLED] ?: false }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    /** Synchronní scrollování všech grafů. Výchozí = true. */
    val syncChartsScroll: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[KEY_SYNC_CHARTS_SCROLL] ?: true }

    suspend fun setSyncChartsScroll(enabled: Boolean) {
        dataStore.edit { it[KEY_SYNC_CHARTS_SCROLL] = enabled }
    }

    /** Banner s výzvou k zapnutí notifikací na dashboardu. Výchozí = false (zobrazit). */
    val notificationBannerDismissed: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[KEY_NOTIFICATION_BANNER_DISMISSED] ?: false }

    suspend fun setNotificationBannerDismissed() {
        dataStore.edit { it[KEY_NOTIFICATION_BANNER_DISMISSED] = true }
    }
}
