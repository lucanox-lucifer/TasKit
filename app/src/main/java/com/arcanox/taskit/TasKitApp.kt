package com.arcanox.taskit

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.arcanox.taskit.util.NotificationHelper
import com.arcanox.taskit.worker.UpdateWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class TasKitApp : Application(), Configuration.Provider {
    
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannels()
        scheduleUpdateChecks()
    }

    private fun scheduleUpdateChecks() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateRequest = PeriodicWorkRequestBuilder<UpdateWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ota_update_check",
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest,
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
