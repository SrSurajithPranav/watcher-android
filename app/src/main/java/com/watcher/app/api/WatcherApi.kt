package com.watcher.app.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface WatcherApi {

    @GET("healthz")
    suspend fun health(): Response<HealthStatus>

    @GET("monitors")
    suspend fun listMonitors(): Response<List<Monitor>>

    @POST("monitors")
    suspend fun createMonitor(@Body input: MonitorInput): Response<Monitor>

    @GET("monitors/{id}")
    suspend fun getMonitor(@Path("id") id: String): Response<Monitor>

    @PATCH("monitors/{id}")
    suspend fun updateMonitor(@Path("id") id: String, @Body update: MonitorUpdate): Response<Monitor>

    @DELETE("monitors/{id}")
    suspend fun deleteMonitor(@Path("id") id: String): Response<Unit>

    @POST("monitors/{id}/check")
    suspend fun checkMonitor(@Path("id") id: String): Response<Monitor>

    @GET("monitors/{id}/history")
    suspend fun getMonitorHistory(@Path("id") id: String): Response<List<HistoryEvent>>

    @GET("dashboard")
    suspend fun getDashboard(): Response<Dashboard>

    @GET("sources")
    suspend fun listSources(): Response<List<SourceStatus>>

    @GET("settings")
    suspend fun getSettings(): Response<Settings>

    @PATCH("settings")
    suspend fun updateSettings(@Body update: SettingsUpdate): Response<Settings>
}
