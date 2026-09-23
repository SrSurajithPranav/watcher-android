package com.watcher.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = WatcherGreen,
    secondary = WatcherGreenLight,
    background = WatcherBg,
    surface = WatcherSurface,
    onSurface = WatcherOnSurface,
    error = WatcherRed,
)

private val DarkColors = darkColorScheme(
    primary = WatcherGreenLight,
    secondary = WatcherGreen,
    background = WatcherBgDark,
    surface = WatcherSurfaceDark,
    onSurface = WatcherOnSurfaceDark,
    error = WatcherRed,
)

@Composable
fun WatcherTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = WatcherTypography,
        content = content,
    )
}
