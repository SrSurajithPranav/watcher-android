package com.watcher.app.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MonitorTarget(
    val source: String,
    val status: String, // available | unavailable | unknown | error | booking_open | booking_closed | no_shows
    val price: Double?,
    val detail: String,
    val checkedAt: String? = null,
    val url: String? = null,
)

@JsonClass(generateAdapter = true)
data class Monitor(
    val id: String,
    val type: String, // product | movie
    val name: String,
    val status: String, // active | paused | triggered | error | completed
    val active: Boolean,
    val intervalMinutes: Int,
    val createdAt: String,
    val lastCheckedAt: String?,
    val nextCheckAt: String?,
    val location: String?,
    val targets: List<MonitorTarget> = emptyList(),
    val lastSummary: String,
)

@JsonClass(generateAdapter = true)
data class MonitorInput(
    val type: String,
    val name: String,
    val brand: String? = null,
    val variant: String? = null,
    val maxPrice: Double? = null,
    val location: String? = null,
    val theatre: String? = null,
    val dates: List<String> = emptyList(),
    val city: String? = null,
    val showtime: String? = null,
    val format: String? = null,
    val intervalMinutes: Int,
)

@JsonClass(generateAdapter = true)
data class MonitorUpdate(
    val active: Boolean? = null,
    val status: String? = null,
    val intervalMinutes: Int? = null,
)

@JsonClass(generateAdapter = true)
data class HistoryEvent(
    val id: String,
    val kind: String,
    val message: String,
    val createdAt: String,
    val source: String?,
)

@JsonClass(generateAdapter = true)
data class Dashboard(
    val activeCount: Int,
    val triggeredCount: Int,
    val unknownCount: Int,
    val recentEvents: List<HistoryEvent> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class SourceStatus(
    val name: String,
    val kind: String, // shopping | movies
    val status: String, // ready | not_configured | blocked | error
    val detail: String,
)

@JsonClass(generateAdapter = true)
data class Settings(
    val defaultLocation: String,
    val intervalMinutes: Int,
    val notifications: Boolean,
    val sound: Boolean,
    val vibration: Boolean,
    val darkMode: Boolean,
)

@JsonClass(generateAdapter = true)
data class SettingsUpdate(
    val defaultLocation: String? = null,
    val intervalMinutes: Int? = null,
    val notifications: Boolean? = null,
    val sound: Boolean? = null,
    val vibration: Boolean? = null,
    val darkMode: Boolean? = null,
)

@JsonClass(generateAdapter = true)
data class HealthStatus(
    @Json(name = "status") val status: String,
)

@JsonClass(generateAdapter = true)
data class ApiError(
    val error: String,
)
