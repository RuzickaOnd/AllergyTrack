package com.allergytrack.shared.config

/**
 * Konfigurace předaná z platformní aplikace.
 *
 * Nahrazuje BuildConfig, který generuje Android Gradle Plugin a v commonMain
 * neexistuje. Instanci vytváří :app v Koin modulu a plní ji z BuildConfig.
 *
 * Používá ji jen kód, který míří do commonMain. Kód zůstávající v :app
 * (UI obrazovky, AppUpdateHelper, NetworkModule) čte BuildConfig dál přímo.
 */
data class AppConfig(
    val supabaseUrl: String,
    val supabaseKey: String,
    val isDebug: Boolean,
    val versionName: String
)
