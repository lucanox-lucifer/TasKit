package com.arcanox.taskit

import android.app.Application
import com.arcanox.taskit.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TasKitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper(this).createNotificationChannel()
    }
}
