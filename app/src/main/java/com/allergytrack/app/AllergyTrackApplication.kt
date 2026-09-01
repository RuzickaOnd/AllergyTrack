package com.allergytrack.app

import android.app.Application
import androidx.work.Configuration
import com.allergytrack.app.data.analytics.AnalyticsManager
import com.allergytrack.app.data.local.datastore.DebugPreferences
import com.allergytrack.app.data.notifications.NotificationManager as AppNotificationManager
import com.allergytrack.app.di.koin.coreModule
import com.allergytrack.app.di.koin.dataStoreModule
import com.allergytrack.app.di.koin.databaseModule
import com.allergytrack.app.di.koin.domainModule
import com.allergytrack.app.di.koin.networkModule
import com.allergytrack.app.di.koin.repositoryModule
import com.allergytrack.app.di.koin.viewModelModule
import com.allergytrack.app.di.koin.workerModule
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AllergyTrackApplication : Application(), Configuration.Provider {

    companion object {
        lateinit var instance: AllergyTrackApplication
            private set
    }

    private val debugPreferences: DebugPreferences by inject()
    private val analyticsManager: AnalyticsManager by inject()
    private val appNotificationManager: AppNotificationManager by inject()

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            if (BuildConfig.DEBUG) androidLogger(Level.ERROR)
            androidContext(this@AllergyTrackApplication)
            workManagerFactory()
            modules(
                coreModule,
                dataStoreModule,
                databaseModule,
                networkModule,
                repositoryModule,
                domainModule,
                viewModelModule,
                workerModule
            )
        }

        appNotificationManager.createNotificationChannels()

        // Inicializace RevenueCat
        Purchases.configure(
            PurchasesConfiguration.Builder(
                this,
                BuildConfig.REVENUECAT_API_KEY
            ).build()
        )

        // Inicializace analytiky na pozadí, aby se neblokovalo hlavní vlákno při startu
        MainScope().launch(Dispatchers.Default) {
            analyticsManager.initialize()
        }

        if (BuildConfig.DEBUG) {
            setupDebugFirebaseToggles()
        }
    }

    private fun setupDebugFirebaseToggles() {
        val scope = MainScope()
        scope.launch {
            debugPreferences.analyticsEnabled.collect { enabled ->
                com.google.firebase.analytics.FirebaseAnalytics
                    .getInstance(this@AllergyTrackApplication)
                    .setAnalyticsCollectionEnabled(enabled)
            }
        }
        scope.launch {
            debugPreferences.crashlyticsEnabled.collect { enabled ->
                com.google.firebase.crashlytics.FirebaseCrashlytics
                    .getInstance()
                    .setCrashlyticsCollectionEnabled(enabled)
            }
        }
    }

    /**
     * workManagerFactory() v startKoin registruje Koin WorkerFactory globálně,
     * takže Configuration stačí prázdná. Rozhraní ale musí zůstat implementované,
     * protože manifest má vypnutý default inicializátor WorkManageru.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}
