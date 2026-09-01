package com.allergytrack.shared.platform

/** Implementuje NotificationManager v :app (Firebase Messaging + kanály). */
interface AppNotifications {
    fun createNotificationChannels()

    /** Callback-based, aby odpovídalo Firebase API — záměrně ne suspend. */
    fun getFcmToken(onComplete: (String?) -> Unit)
}
