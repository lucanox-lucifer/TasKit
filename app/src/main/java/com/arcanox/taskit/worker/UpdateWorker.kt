package com.arcanox.taskit.worker

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arcanox.taskit.data.repository.UpdateRepository
import com.arcanox.taskit.util.UpdateNotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

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
            val update = result.getOrNull() ?: return Result.failure()
            
            val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                applicationContext.packageManager
                    .getPackageInfo(applicationContext.packageName, 0).longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                applicationContext.packageManager
                    .getPackageInfo(applicationContext.packageName, 0).versionCode
            }
            
            if (update.versionCode > currentVersionCode) {
                notificationHelper.showUpdateAvailable(update.versionName, update.mandatory)
            }
        }
        return Result.success()
    }
}
