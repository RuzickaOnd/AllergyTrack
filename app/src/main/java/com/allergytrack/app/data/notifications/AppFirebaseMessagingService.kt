package com.allergytrack.app.data.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Služba na pozadí, která přijímá zprávy a tokeny z Firebase Cloud Messaging (FCM).
 */
class AppFirebaseMessagingService : FirebaseMessagingService() {

    private val notificationManager: NotificationManager by inject()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private const val TAG = "AppFCMService"
    }

    /**
     * Volá se při doručení nové FCM zprávy na zařízení (appka v popředí / data payload).
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Přijata FCM zpráva od: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"]

        scope.launch {
            notificationManager.showPushNotification(title, body, remoteMessage.data)
        }
    }

    /**
     * Volá se, pokud byl vygenerován nový nebo aktualizovaný FCM registrační token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nový FCM token vygenerován: $token")
        
        // Zde by v produkci proběhlo odeslání tokenu na backend API přes repository
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
