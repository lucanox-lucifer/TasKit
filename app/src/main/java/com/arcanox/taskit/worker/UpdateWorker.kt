package com.arcanox.taskit.worker

import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arcanox.taskit.data.repository.UpdateRepository
import com.arcanox.taskit.util.UpdateNotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class UpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: UpdateRepository,
    private val notificationHelper: UpdateNotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = repository.checkUpdate()
        if (result.isSuccess) {
            val prefsData = repository.getLocalUpdateData().first()
            
            val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                applicationContext.packageManager
                    .getPackageInfo(applicationContext.packageName, 0).longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                applicationContext.packageManager
                    .getPackageInfo(applicationContext.packageName, 0).versionCode
            }
            
            if (prefsData.latestVersionCode > currentVersionCode) {
                var daysLeft: Int? = null
                if (prefsData.mandatory) {
                    if (prefsData.firstDetectedTime > 0) {
                        val elapsed = System.currentTimeMillis() - prefsData.firstDetectedTime
                        daysLeft = (7 - (elapsed / (1000 * 60 * 60 * 24))).toInt().coerceAtLeast(0)
                    }
                }
                notificationHelper.showUpdateAvailable(
                    prefsData.latestVersionName,
                    prefsData.mandatory,
                    daysLeft,
                )
            }
        }
        return Result.success()
    }
}
