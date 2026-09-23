package com.watcher.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.watcher.app.api.MonitorInput
import com.watcher.app.ui.WatcherViewModel

@Composable
fun AddMonitorScreen(viewModel: WatcherViewModel, onDone: () -> Unit, onCancel: () -> Unit) {
    var mode by remember { mutableStateOf<String?>(null) }

    when (mode) {
        null -> ModePickerScreen(onPick = { mode = it }, onCancel = onCancel)
        "product" -> ProductForm(viewModel, onDone = onDone, onBack = { mode = null })
        "movie" -> MovieForm(viewModel, onDone = onDone, onBack = { mode = null })
    }
}

@Composable
private fun ModePickerScreen(onPick: (String) -> Unit, onCancel: () -> Unit) {
    Scaffold(topBar = { TopBar("Add Monitor", onCancel) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            ChoiceCard(
                icon = Icons.Filled.ShoppingCart,
                title = "Product",
                subtitle = "Groceries, electronics, anything sold online",
                onClick = { onPick("product") },
            )
            Spacer(Modifier.height(16.dp))
            ChoiceCard(
                icon = Icons.Filled.LocalMovies,
                title = "Movie",
                subtitle = "Ticket booking for a movie and dates",
                onClick = { onPick("movie") },
            )
        }
    }
}

@Composable
private fun ChoiceCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.height(36.dp))
            Spacer(Modifier.height(0.dp))
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = { TextButton(onClick = onBack) { Text("Cancel") } },
    )
}

@Composable
private fun ProductForm(viewModel: WatcherViewModel, onDone: () -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var variant by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showAdvanced by remember { mutableStateOf(false) }
    var interval by remember { mutableStateOf("10") }
    var error by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopBar("Product Monitor", onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(name, { name = it }, label = { Text("Product name *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(brand, { brand = it }, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(variant, { variant = it }, label = { Text("Variant / Size") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(maxPrice, { maxPrice = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Maximum price (\u20B9)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(location, { location = it }, label = { Text("Location / Pincode") }, modifier = Modifier.fillMaxWidth())

            TextButton(onClick = { showAdvanced = !showAdvanced }) {
                Text(if (showAdvanced) "Hide advanced" else "Advanced")
            }
            if (showAdvanced) {
                OutlinedTextField(
                    interval,
                    { interval = it.filter { c -> c.isDigit() } },
                    label = { Text("Check interval (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Source selection is decided automatically based on the product category (grocery/quick-commerce vs. general retail), per the backend's routing rules.",
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                enabled = name.isNotBlank() && !submitting,
                onClick = {
                    submitting = true
                    error = null
                    val input = MonitorInput(
                        type = "product",
                        name = name.trim(),
                        brand = brand.ifBlank { null },
                        variant = variant.ifBlank { null },
                        maxPrice = maxPrice.toDoubleOrNull(),
                        location = location.ifBlank { null },
                        intervalMinutes = interval.toIntOrNull()?.coerceIn(5, 1440) ?: 10,
                    )
                    viewModel.createMonitor(input) { ok, message ->
                        submitting = false
                        if (ok) onDone() else error = message
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (submitting) "Starting..." else "Search automatically") }
        }
    }
}

@Composable
private fun MovieForm(viewModel: WatcherViewModel, onDone: () -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var theatre by remember { mutableStateOf("") }
    var datesText by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var showtime by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("") }
    var interval by remember { mutableStateOf("10") }
    var error by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopBar("Movie Monitor", onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(name, { name = it }, label = { Text("Movie name *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(theatre, { theatre = it }, label = { Text("Theatre (optional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                datesText,
                { datesText = it },
                label = { Text("Date(s) * \u2014 e.g. 02/10/2026, 03/10/2026") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(city, { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(showtime, { showtime = it }, label = { Text("Preferred showtime") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(format, { format = it }, label = { Text("Preferred format (2D/3D/IMAX)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                interval,
                { interval = it.filter { c -> c.isDigit() } },
                label = { Text("Check interval (minutes)") },
                modifier = Modifier.fillMaxWidth(),
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                enabled = name.isNotBlank() && datesText.isNotBlank() && !submitting,
                onClick = {
                    submitting = true
                    error = null
                    val dates = datesText.split(",", "\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val input = MonitorInput(
                        type = "movie",
                        name = name.trim(),
                        theatre = theatre.ifBlank { null },
                        dates = dates,
                        city = city.ifBlank { null },
                        showtime = showtime.ifBlank { null },
                        format = format.ifBlank { null },
                        intervalMinutes = interval.toIntOrNull()?.coerceIn(5, 1440) ?: 10,
                    )
                    viewModel.createMonitor(input) { ok, message ->
                        submitting = false
                        if (ok) onDone() else error = message
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (submitting) "Starting..." else "Start Monitoring") }
        }
    }
}
