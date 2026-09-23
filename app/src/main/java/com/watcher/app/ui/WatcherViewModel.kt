package com.watcher.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.watcher.app.api.ApiClient
import com.watcher.app.api.Dashboard
import com.watcher.app.api.HistoryEvent
import com.watcher.app.api.Monitor
import com.watcher.app.api.MonitorInput
import com.watcher.app.api.MonitorUpdate
import com.watcher.app.api.Settings
import com.watcher.app.api.SettingsUpdate
import com.watcher.app.api.SourceStatus
import com.watcher.app.data.AppPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoadState {
    object Idle : LoadState
    object Loading : LoadState
    data class Error(val message: String) : LoadState
    object Loaded : LoadState
}

class WatcherViewModel(application: Application) : AndroidViewModel(application) {

    private val _monitors = MutableStateFlow<List<Monitor>>(emptyList())
    val monitors: StateFlow<List<Monitor>> = _monitors.asStateFlow()

    private val _dashboard = MutableStateFlow<Dashboard?>(null)
    val dashboard: StateFlow<Dashboard?> = _dashboard.asStateFlow()

    private val _sources = MutableStateFlow<List<SourceStatus>>(emptyList())
    val sources: StateFlow<List<SourceStatus>> = _sources.asStateFlow()

    private val _settings = MutableStateFlow<Settings?>(null)
    val settings: StateFlow<Settings?> = _settings.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryEvent>>(emptyList())
    val history: StateFlow<List<HistoryEvent>> = _history.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    private val _baseUrl = MutableStateFlow(AppPrefs.DEFAULT_BASE_URL)
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    init {
        viewModelScope.launch {
            AppPrefs.baseUrlFlow(getApplication()).collect { _baseUrl.value = it }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _loadState.value = LoadState.Loading
            try {
                val api = ApiClient.get(getApplication())
                val monitorsResp = api.listMonitors()
                if (monitorsResp.isSuccessful) _monitors.value = monitorsResp.body().orEmpty()

                val dashResp = api.getDashboard()
                if (dashResp.isSuccessful) _dashboard.value = dashResp.body()

                val sourcesResp = api.listSources()
                if (sourcesResp.isSuccessful) _sources.value = sourcesResp.body().orEmpty()

                val settingsResp = api.getSettings()
                if (settingsResp.isSuccessful) _settings.value = settingsResp.body()

                _loadState.value = LoadState.Loaded
            } catch (e: Exception) {
                _loadState.value = LoadState.Error(e.message ?: "Could not reach backend")
            }
        }
    }

    fun createMonitor(input: MonitorInput, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val api = ApiClient.get(getApplication())
                val resp = api.createMonitor(input)
                if (resp.isSuccessful) {
                    refreshAll()
                    onDone(true, null)
                } else {
                    onDone(false, "Server rejected the monitor (${resp.code()})")
                }
            } catch (e: Exception) {
                onDone(false, e.message ?: "Network error")
            }
        }
    }

    fun setActive(id: String, active: Boolean) {
        viewModelScope.launch {
            try {
                val api = ApiClient.get(getApplication())
                api.updateMonitor(id, MonitorUpdate(active = active))
                refreshAll()
            } catch (_: Exception) { /* surfaced via next refresh error state */ }
        }
    }

    fun deleteMonitor(id: String) {
        viewModelScope.launch {
            try {
                val api = ApiClient.get(getApplication())
                api.deleteMonitor(id)
                refreshAll()
            } catch (_: Exception) { }
        }
    }

    fun checkNow(id: String) {
        viewModelScope.launch {
            try {
                val api = ApiClient.get(getApplication())
                api.checkMonitor(id)
                refreshAll()
            } catch (_: Exception) { }
        }
    }

    fun loadHistory(id: String) {
        viewModelScope.launch {
            try {
                val api = ApiClient.get(getApplication())
                val resp = api.getMonitorHistory(id)
                if (resp.isSuccessful) _history.value = resp.body().orEmpty()
            } catch (_: Exception) { _history.value = emptyList() }
        }
    }

    fun updateSettings(update: SettingsUpdate) {
        viewModelScope.launch {
            try {
                val api = ApiClient.get(getApplication())
                val resp = api.updateSettings(update)
                if (resp.isSuccessful) _settings.value = resp.body()
            } catch (_: Exception) { }
        }
    }

    fun setBaseUrl(url: String) {
        viewModelScope.launch {
            AppPrefs.setBaseUrl(getApplication(), url)
            ApiClient.invalidate()
            refreshAll()
        }
    }

    fun monitorById(id: String): Monitor? = _monitors.value.find { it.id == id }
}
