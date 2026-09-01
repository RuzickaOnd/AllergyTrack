package com.allergytrack.app.di.koin

import com.allergytrack.app.data.local.datastore.DebugPreferences
import com.allergytrack.app.data.local.datastore.OnboardingDataStore
import com.allergytrack.app.data.local.datastore.UserPreferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataStoreModule = module {
    single { OnboardingDataStore(androidContext()) }
    single { UserPreferencesDataStore(androidContext()) }
    single { DebugPreferences(androidContext()) }
}
