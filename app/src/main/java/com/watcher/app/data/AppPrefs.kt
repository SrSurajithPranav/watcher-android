package com.watcher.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "watcher_prefs")

/**
 * Stores the base URL of the user's own Watcher backend (the Express/Postgres
 * server already deployed from the Watcher repo, e.g. a Replit deployment URL).
 * This app is a control panel client for that backend; it does not embed or
 * fake its own scraping logic.
 */
object AppPrefs {
    private val BASE_URL_KEY = stringPreferencesKey("base_url")
    const val DEFAULT_BASE_URL = "https://YOUR-WATCHER-BACKEND.example.com/api/"

    fun baseUrlFlow(context: Context): Flow<String> =
        context.dataStore.data.map { prefs -> prefs[BASE_URL_KEY] ?: DEFAULT_BASE_URL }

    suspend fun setBaseUrl(context: Context, url: String) {
        val normalized = if (url.endsWith("/")) url else "$url/"
        context.dataStore.edit { prefs -> prefs[BASE_URL_KEY] = normalized }
    }
}
