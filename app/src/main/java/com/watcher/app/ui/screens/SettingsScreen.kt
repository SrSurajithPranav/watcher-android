package com.watcher.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.watcher.app.api.SettingsUpdate
import com.watcher.app.ui.WatcherViewModel
import com.watcher.app.ui.components.StatusDot

@Composable
fun SettingsScreen(viewModel: WatcherViewModel) {
    val settings by viewModel.settings.collectAsState()
    val sources by viewModel.sources.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()

    var urlField by remember(baseUrl) { mutableStateOf(baseUrl) }
    var showAdvanced by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refreshAll() }

    Scaffold(topBar = { TopAppBar(title = { androidx.compose.material3.Text("Settings") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Backend", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = urlField,
                onValueChange = { urlField = it },
                label = { Text("Backend base URL") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = { viewModel.setBaseUrl(urlField) }) { Text("Save & reconnect") }

            HorizontalDivider()

            settings?.let { s ->
                Text("Defaults", style = MaterialTheme.typography.titleMedium)
                var location by remember(s) { mutableStateOf(s.defaultLocation) }
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Default location / pincode") },
                    modifier = Modifier.fillMaxWidth(),
                )
                var interval by remember(s) { mutableStateOf(s.intervalMinutes.toString()) }
                OutlinedTextField(
                    value = interval,
                    onValueChange = { interval = it.filter { c -> c.isDigit() } },
                    label = { Text("Check interval (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                ToggleRow("Notifications", s.notifications) { viewModel.updateSettings(SettingsUpdate(notifications = it)) }
                ToggleRow("Sound", s.sound) { viewModel.updateSettings(SettingsUpdate(sound = it)) }
                ToggleRow("Vibration", s.vibration) { viewModel.updateSettings(SettingsUpdate(vibration = it)) }
                ToggleRow("Dark mode", s.darkMode) { viewModel.updateSettings(SettingsUpdate(darkMode = it)) }

                Button(onClick = {
                    viewModel.updateSettings(
                        SettingsUpdate(
                            defaultLocation = location.ifBlank { null },
                            intervalMinutes = interval.toIntOrNull()?.coerceIn(5, 1440),
                        ),
                    )
                }) { Text("Save defaults") }
            }

            HorizontalDivider()
            Text(
                "Notifications on this phone fire from a background sync that runs roughly every 15\u201330 minutes " +
                    "(Android's own limit for periodic background work, and it can be delayed further by battery optimization). " +
                    "The backend itself checks every few minutes but has no push service wired up yet, so this is the honest " +
                    "current behavior rather than a guaranteed real-time push.",
                style = MaterialTheme.typography.labelSmall,
            )

            HorizontalDivider()
            TextButton(onClick = { showAdvanced = !showAdvanced }) {
                Text(if (showAdvanced) "Hide advanced" else "Advanced: backend status")
            }
            if (showAdvanced) {
                if (sources.isEmpty()) {
                    Text("Could not load source status.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    sources.forEach { src ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            StatusDot(src.status, modifier = Modifier.padding(end = 10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(src.name, style = MaterialTheme.typography.bodyMedium)
                                Text(src.detail, style = MaterialTheme.typography.labelSmall)
                            }
                            Text(src.status, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
