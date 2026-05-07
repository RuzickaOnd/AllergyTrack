package com.allergytrack.app.data.worker

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleEnvironmentTracking() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<EnvironmentWorker>(
            repeatInterval = 30,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EnvironmentWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelEnvironmentTracking() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(EnvironmentWorker.WORK_NAME)
    }

    /**
     * Naplánuje večerní připomínku k logování jako periodickou WorkManager úlohu.
     *
     * Initial delay se počítá na nejbližší 20:00 místního času — pokud je aktuální čas
     * po 20:00, naplánuje se na 20:00 následujícího dne. Poté se opakuje každých 24 hodin.
     *
     * Používá [ExistingPeriodicWorkPolicy.REPLACE]: při opakovaném zapnutí notifikací
     * se timing resetuje na nejbližší 20:00 (zabraňuje driftu z předchozích plánování).
     */
    fun scheduleReminder() {
        val now = Calendar.getInstance()
        val next8pm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = next8pm.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    /** Zruší naplánovanou večerní připomínku. Okamžitě účinné, přežije restart. */
    fun cancelReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(ReminderWorker.WORK_NAME)
    }
}
