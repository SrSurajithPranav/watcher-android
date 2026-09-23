package com.watcher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watcher.app.ui.theme.WatcherAmber
import com.watcher.app.ui.theme.WatcherGreenLight
import com.watcher.app.ui.theme.WatcherRed

/** Maps a backend status string to a color + short label, matching the spec's 🟢🟡🔴⚪ scheme. */
fun statusColor(status: String): Color = when (status) {
    "available", "booking_open" -> WatcherGreenLight
    "unknown" -> Color(0xFF9AA5A0)
    "error", "unavailable", "booking_closed", "no_shows" -> WatcherRed
    "triggered" -> WatcherGreenLight
    "active" -> WatcherAmber
    "paused" -> Color(0xFF9AA5A0)
    else -> Color(0xFF9AA5A0)
}

@Composable
fun StatusDot(status: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .background(statusColor(status), shape = RoundedCornerShape(50)),
    )
}

@Composable
fun StatusPill(text: String, status: String) {
    Box(
        modifier = Modifier
            .background(statusColor(status).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(text = text, color = statusColor(status), fontSize = 12.sp)
    }
}

@Composable
fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}
