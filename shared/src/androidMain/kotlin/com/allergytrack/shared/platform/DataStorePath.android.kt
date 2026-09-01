package com.allergytrack.shared.platform

import android.content.Context

private lateinit var appContext: Context

/** Volá se z AllergyTrackApplication.onCreate() PŘED startKoin. */
fun initAndroidPlatform(context: Context) {
    appContext = context.applicationContext
}

/** Application context pro platformní implementace v androidMain. */
fun applicationContext(): Context = appContext

/**
 * Reprodukuje cestu, kterou používal androidx delegát preferencesDataStore(name):
 *   filesDir/datastore/<name>.preferences_pb
 * Ověřeno na zařízení — viz docs/kmp-migration-baseline.md.
 */
actual fun dataStorePath(fileName: String): String =
    appContext.filesDir.resolve("datastore/$fileName.preferences_pb").absolutePath
