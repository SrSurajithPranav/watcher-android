package com.watcher.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.watcher.app.api.HistoryEvent
import com.watcher.app.ui.WatcherViewModel
import com.watcher.app.ui.components.EmptyState

@Composable
fun HistoryScreen(viewModel: WatcherViewModel) {
    val dashboard by viewModel.dashboard.collectAsState()
    val events = dashboard?.recentEvents.orEmpty()

    Scaffold(topBar = { TopAppBar(title = { Text("History") }) }) { padding ->
        if (events.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding)) {
                EmptyState("No activity yet", "Events across all monitors will show up here.")
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 8.dp, 16.dp, 24.dp)) {
                items(events) { event -> HistoryEventRow(event) }
            }
        }
    }
}

@Composable
private fun HistoryEventRow(event: HistoryEvent) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(event.createdAt, style = MaterialTheme.typography.labelSmall)
        Text(event.message, style = MaterialTheme.typography.bodyMedium)
        event.source?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
    }
}
