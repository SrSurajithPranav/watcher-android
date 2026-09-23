package com.watcher.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.watcher.app.notify.WatcherSyncWorker
import com.watcher.app.ui.WatcherViewModel
import com.watcher.app.ui.nav.WatcherNavHost
import com.watcher.app.ui.theme.WatcherTheme

class MainActivity : ComponentActivity() {

    private val viewModel: WatcherViewModel by viewModels()

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* if denied, notifications simply won't show; app still functions as a control panel */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        WatcherSyncWorker.schedule(this)
        viewModel.refreshAll()

        setContent {
            val settings by viewModel.settings.collectAsState()
            WatcherTheme(darkTheme = settings?.darkMode ?: false) {
                WatcherNavHost(viewModel)
            }
        }
    }
}
