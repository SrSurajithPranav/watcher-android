package com.watcher.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.watcher.app.api.HistoryEvent
import com.watcher.app.api.Monitor
import com.watcher.app.api.MonitorTarget
import com.watcher.app.ui.WatcherViewModel
import com.watcher.app.ui.components.StatusDot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorDetailScreen(viewModel: WatcherViewModel, monitorId: String, onBack: () -> Unit) {
    val monitors by viewModel.monitors.collectAsState()
    val history by viewModel.history.collectAsState()
    val monitor = monitors.find { it.id == monitorId }

    LaunchedEffect(monitorId) { viewModel.loadHistory(monitorId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(monitor?.name ?: "Monitor") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
                actions = {
                    IconButton(onClick = { viewModel.checkNow(monitorId) }) {
                        Icon(androidx.compose.material.icons.Icons.Filled.Refresh, contentDescription = "Check now")
                    }
                },
            )
        },
    ) { padding ->
        if (monitor == null) {
            Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                Text("Loading\u2026", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            return@Scaffold
        }

        LazyColumn(contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 8.dp, 16.dp, 24.dp)) {
            item { MonitorMeta(monitor, onToggleActive = { viewModel.setActive(monitor.id, it) }, onDelete = { viewModel.deleteMonitor(monitor.id); onBack() }) }
            item { Text("Targets", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp)) }
            items(monitor.targets) { target -> TargetRow(target) }
            item { Text("History", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp)) }
            if (history.isEmpty()) {
                item { Text("No events yet.", style = MaterialTheme.typography.bodyMedium) }
            } else {
                items(history) { event -> HistoryRow(event) }
            }
        }
    }
}

@Composable
private fun MonitorMeta(monitor: Monitor, onToggleActive: (Boolean) -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Status: ${monitor.status}", style = MaterialTheme.typography.bodyMedium)
                    Text("Last checked: ${monitor.lastCheckedAt ?: "\u2014"}", style = MaterialTheme.typography.labelSmall)
                    Text("Next check: ${monitor.nextCheckAt ?: "\u2014"}", style = MaterialTheme.typography.labelSmall)
                }
                Switch(checked = monitor.active, onCheckedChange = onToggleActive)
            }
            TextButton(onClick = onDelete) { Text("Delete monitor", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun TargetRow(target: MonitorTarget) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusDot(target.status, modifier = Modifier.padding(end = 10.dp))
        Column(Modifier.weight(1f)) {
            Text(target.source, style = MaterialTheme.typography.bodyLarge)
            Text(target.detail, style = MaterialTheme.typography.labelSmall)
        }
        target.price?.let { Text("\u20B9${it.toInt()}", style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun HistoryRow(event: HistoryEvent) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(event.createdAt, style = MaterialTheme.typography.labelSmall)
        Text(event.message, style = MaterialTheme.typography.bodyMedium)
    }
}
