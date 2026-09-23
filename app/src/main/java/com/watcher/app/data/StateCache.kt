package com.watcher.app.data

import android.content.Context

/**
 * Plain SharedPreferences (not DataStore) since this is called from a
 * background Worker and needs simple synchronous reads/writes.
 * Key: "<monitorId>:<source>" -> last-seen status string.
 */
object StateCache {
    private const val PREFS = "watcher_state_cache"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun lastStatus(context: Context, monitorId: String, source: String): String? =
        prefs(context).getString("$monitorId:$source", null)

    fun setStatus(context: Context, monitorId: String, source: String, status: String) {
        prefs(context).edit().putString("$monitorId:$source", status).apply()
    }

    /** Positive/interesting states worth notifying on. */
    private val POSITIVE_STATES = setOf("available", "booking_open")

    /** States that are considered "not yet" so a transition into a positive state is meaningful. */
    private val NEGATIVE_STATES = setOf("unavailable", "unknown", "error", "booking_closed", "no_shows")

    fun isMeaningfulTransition(previous: String?, current: String): Boolean {
        if (current !in POSITIVE_STATES) return false
        // No prior reading is treated like a negative baseline: if the very first check
        // already finds it available/booking_open, that is itself the event worth surfacing.
        return previous == null || previous in NEGATIVE_STATES
    }
}
