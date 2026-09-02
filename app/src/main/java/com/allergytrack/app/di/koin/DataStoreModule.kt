package com.allergytrack.app.di.koin

import com.allergytrack.shared.data.local.datastore.DebugPreferences
import com.allergytrack.shared.data.local.datastore.OnboardingDataStore
import com.allergytrack.shared.data.local.datastore.UserPreferencesDataStore
import com.allergytrack.shared.data.local.datastore.createDataStore
import org.koin.dsl.module

/**
 * Názvy souborů drží samotné třídy v FILE_NAME a nesmí se změnit —
 * jinak by uživatelé přišli o nastavení. Viz dataStorePath().
 */
val dataStoreModule = module {
    single { UserPreferencesDataStore(createDataStore(UserPreferencesDataStore.FILE_NAME)) }
    single { OnboardingDataStore(createDataStore(OnboardingDataStore.FILE_NAME)) }
    single { DebugPreferences(createDataStore(DebugPreferences.FILE_NAME)) }
}
