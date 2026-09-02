package com.allergytrack.shared.platform

/** Implementuje WorkManagerScheduler v :app (WorkManager). */
interface BackgroundScheduler {
    fun scheduleEnvironmentTracking()
    fun cancelEnvironmentTracking()
    fun scheduleReminder()
    fun cancelReminder()
}
