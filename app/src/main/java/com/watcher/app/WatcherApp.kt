package com.watcher.app

import android.app.Application
import com.watcher.app.notify.Notifications

class WatcherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.ensureChannel(this)
    }
}
