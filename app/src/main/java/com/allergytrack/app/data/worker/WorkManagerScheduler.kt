package com.allergytrack.app.data.worker

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WorkManagerScheduler constructor(
    private val context: Context
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
     * Initial delay se počítá na nejbližší 19:00 místního času — pokud je aktuální čas
     * po 19:00, naplánuje se na 19:00 následujícího dne. Poté se opakuje každých 24 hodin.
     *
     * Používá [ExistingPeriodicWorkPolicy.REPLACE]: při opakovaném zapnutí notifikací
     * se timing resetuje na nejbližší 19:00 (zabraňuje driftu z předchozích plánování).
     */
    fun scheduleReminder() {
        val now = Calendar.getInstance()
        val next7pm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = next7pm.timeInMillis - now.timeInMillis

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
