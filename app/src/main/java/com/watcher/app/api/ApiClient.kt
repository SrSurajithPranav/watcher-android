package com.watcher.app.api

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.watcher.app.data.AppPrefs
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private var cachedBaseUrl: String? = null
    private var cachedApi: WatcherApi? = null

    /** Returns a WatcherApi bound to whatever base URL is currently saved in prefs. */
    suspend fun get(context: Context): WatcherApi {
        val baseUrl = AppPrefs.baseUrlFlow(context.applicationContext).first()
        val existing = cachedApi
        if (existing != null && cachedBaseUrl == baseUrl) return existing

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val api = retrofit.create(WatcherApi::class.java)
        cachedBaseUrl = baseUrl
        cachedApi = api
        return api
    }

    /** Call after the user changes the backend URL in Settings so the next call picks it up. */
    fun invalidate() {
        cachedApi = null
        cachedBaseUrl = null
    }
}
