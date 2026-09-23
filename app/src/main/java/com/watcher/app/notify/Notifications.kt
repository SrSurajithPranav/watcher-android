package com.watcher.app.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.watcher.app.MainActivity
import com.watcher.app.R

/**
 * Local (on-device) notifications only.
 *
 * IMPORTANT — read this before assuming push works:
 * There is no server-side push wired up. The existing backend has no
 * /device/register endpoint and no Firebase Admin credentials configured,
 * so it cannot push to this app while it is closed or the phone is asleep.
 * These notifications fire only when WatcherSyncWorker runs (see notify/WatcherSyncWorker.kt),
 * which Android's WorkManager schedules on a best-effort ~15-30 min cadence subject to
 * Doze/battery-optimization — not the reliable 10-minute server push the spec describes.
 * Wiring real push requires: a Firebase project, a /device/register endpoint on the
 * backend, and the backend calling Firebase Admin when it detects a transition itself.
 */
object Notifications {
    const val CHANNEL_ID = "watcher_alerts"
    private const val CHANNEL_NAME = "Availability alerts"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Fires when a watched product becomes available or booking opens"
                enableVibration(true)
                enableLights(true)
            }
            manager?.createNotificationChannel(channel)
        }
    }

    fun notify(context: Context, id: Int, title: String, text: String, monitorId: String) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("monitorId", monitorId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            id,
            openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
