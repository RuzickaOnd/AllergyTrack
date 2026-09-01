package com.allergytrack.shared.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.allergytrack.shared.platform.dataStorePath
import okio.Path.Companion.toPath

/**
 * Nahrazuje androidx delegát preferencesDataStore(name).
 * Cesta k souboru musí odpovídat té původní — viz dataStorePath().
 */
fun createDataStore(fileName: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { dataStorePath(fileName).toPath() }
    )
