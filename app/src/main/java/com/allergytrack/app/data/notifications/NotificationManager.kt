package com.allergytrack.app.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager as SystemNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.allergytrack.app.MainActivity
import com.allergytrack.app.R
import com.allergytrack.shared.data.local.datastore.UserPreferencesDataStore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.firstOrNull

/**
 * Centrální manažer pro správu, konfiguraci a zobrazování notifikací (lokálních i FCM push notifikací).
 */
class NotificationManager(
    private val context: Context,
    private val userPreferences: UserPreferencesDataStore
) {

    companion object {
        const val REMINDER_CHANNEL_ID = "reminder_channel"
        const val FCM_CHANNEL_ID = "fcm_push_channel"
        private const val FCM_NOTIFICATION_ID_BASE = 2000
    }

    /**
     * Inicializuje všechny notifikační kanály.
     * Volá se při startu aplikace v AllergyTrackApplication.
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as SystemNotificationManager

            // 1. Kanál pro denní připomenutí (Local Reminders)
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                context.getString(R.string.notification_channel_reminders_name),
                SystemNotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_reminders_desc)
            }
            systemManager.createNotificationChannel(reminderChannel)

            // 2. Kanál pro Firebase Push notifikace (Zprávy a upozornění na pyl apod.)
            val fcmChannel = NotificationChannel(
                FCM_CHANNEL_ID,
                context.getString(R.string.notification_channel_fcm_name),
                SystemNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_fcm_desc)
                enableLights(true)
                enableVibration(true)
            }
            systemManager.createNotificationChannel(fcmChannel)
        }
    }

    /**
     * Zobrazí příchozí push notifikaci.
     */
    suspend fun showPushNotification(title: String?, body: String?, data: Map<String, String>) {
        // Ověříme, zda má uživatel notifikace povolené v nastavení aplikace
        val isEnabled = userPreferences.notificationsEnabled.firstOrNull() ?: true
        if (!isEnabled) return

        // Otevře MainActivity po kliknutí na notifikaci
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, FCM_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title ?: context.getString(R.string.app_name))
            .setContentText(body ?: "")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationId = FCM_NOTIFICATION_ID_BASE + (System.currentTimeMillis() % 1000).toInt()
        
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    /**
     * Získá aktuální FCM token pro registraci zařízení.
     */
    fun getFcmToken(onComplete: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(task.result)
            } else {
                onComplete(null)
            }
        }
    }
}
