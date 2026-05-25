package com.allergytrack.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.allergytrack.app.BuildConfig
import com.allergytrack.app.data.local.datastore.DebugPreferences
import com.allergytrack.app.data.worker.ReminderWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AllergyTrackApplication : Application(), Configuration.Provider {

    companion object {
        lateinit var instance: AllergyTrackApplication
            private set
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var debugPreferences: DebugPreferences

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        
        if (BuildConfig.DEBUG) {
            setupDebugFirebaseToggles()
        }
    }

    private fun setupDebugFirebaseToggles() {
        val scope = kotlinx.coroutines.MainScope()
        scope.launch {
            debugPreferences.analyticsEnabled.collect { enabled ->
                com.google.firebase.analytics.FirebaseAnalytics.getInstance(this@AllergyTrackApplication)
                    .setAnalyticsCollectionEnabled(enabled)
            }
        }
        scope.launch {
            debugPreferences.crashlyticsEnabled.collect { enabled ->
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .setCrashlyticsCollectionEnabled(enabled)
            }
        }
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                ReminderWorker.CHANNEL_ID,
                getString(R.string.notification_channel_reminders_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_reminders_desc)
            }
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
