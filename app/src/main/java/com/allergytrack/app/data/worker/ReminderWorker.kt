package com.allergytrack.app.data.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.allergytrack.app.MainActivity
import com.allergytrack.app.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker odesílající večerní připomínku k zalogování jídla a příznaků.
 *
 * ## Kdy se notifikace zobrazí
 * Worker je naplánován jako periodická úloha s intervalem 24 hodin.
 * Při prvním zapnutí se spustí s initial delay na nejbližší 20:00 místního času
 * (viz [WorkManagerScheduler.scheduleReminder]). Každý další den přibližně ve 20:00.
 * WorkManager nezaručuje přesný čas — může se zpozdit o desítky minut podle
 * stavu baterie a Doze režimu Androidu.
 *
 * ## Co notifikace zobrazuje
 * - **Ikona:** launcher ikona aplikace
 * - **Název:** "AllergyTrack"
 * - **Text:** "Nezapomeň zalogovat dnešní jídlo a příznaky."
 * - **Akce při kliknutí:** otevře MainActivity (Dashboard), smaže notifikaci
 *
 * ## Kanál
 * `reminder_channel` — IMPORTANCE_DEFAULT (zvuk + oznámení, bez vyskakovacího okna).
 * Kanál je registrován v AllergyTrackApplication.onCreate().
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_reminder_title))
            .setContentText(context.getString(R.string.notification_reminder_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "reminder_worker"
        const val CHANNEL_ID = "reminder_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
