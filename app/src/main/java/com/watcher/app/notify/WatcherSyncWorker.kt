package com.watcher.app.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.watcher.app.api.ApiClient
import com.watcher.app.data.StateCache
import java.util.concurrent.TimeUnit

/**
 * Fetches the current monitor list and, for each target, compares its status against
 * the last-seen status cached on this device. Fires a local high-priority notification
 * on a meaningful transition (see StateCache.isMeaningfulTransition), then updates the cache.
 *
 * This does NOT run the actual product/movie checks — the backend scheduler already
 * does that every 10 minutes server-side (see artifacts/api-server/src/services/scheduler.ts
 * in the Watcher repo). This worker only *reads* the backend's results and turns a
 * meaningful change into a phone notification, since the backend itself has no push
 * integration yet.
 *
 * Reliability note: WorkManager's minimum periodic interval is 15 minutes, and Android's
 * Doze/App Standby can delay it further. That is a real OS constraint, not something this
 * app can override — it is surfaced honestly in Settings rather than claimed as guaranteed.
 */
class WatcherSyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val api = ApiClient.get(applicationContext)
            val response = api.listMonitors()
            if (!response.isSuccessful) return Result.retry()
            val monitors = response.body().orEmpty()

            var notificationId = 1000
            for (monitor in monitors) {
                if (!monitor.active) continue
                for (target in monitor.targets) {
                    val previous = StateCache.lastStatus(applicationContext, monitor.id, target.source)
                    if (StateCache.isMeaningfulTransition(previous, target.status)) {
                        val title = if (target.status == "booking_open") {
                            "\uD83C\uDFAC Booking open"
                        } else {
                            "\uD83D\uDD25 Available"
                        }
                        val priceText = target.price?.let { " · \u20B9${it.toInt()}" } ?: ""
                        Notifications.notify(
                            applicationContext,
                            notificationId++,
                            "$title: ${monitor.name}",
                            "${target.source}$priceText \u2014 ${target.detail}",
                            monitor.id,
                        )
                    }
                    StateCache.setStatus(applicationContext, monitor.id, target.source, target.status)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "watcher_sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WatcherSyncWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}
