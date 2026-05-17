package com.arcanox.taskit.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.arcanox.taskit.R
import com.arcanox.taskit.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createUpdateChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val updateChannel = NotificationChannel(
                UPDATE_CHANNEL_ID,
                "App Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for app updates"
            }

            val mandatoryChannel = NotificationChannel(
                MANDATORY_CHANNEL_ID,
                "Mandatory Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for mandatory updates"
            }

            notificationManager.createNotificationChannel(updateChannel)
            notificationManager.createNotificationChannel(mandatoryChannel)
        }
    }

    fun showUpdateAvailable(version: String, isMandatory: Boolean, daysLeft: Int? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val title = if (isMandatory) "Mandatory Update Available" else "Update Available"
        val message = "TasKit version $version is ready to install." + 
            (if (daysLeft != null) " $daysLeft days remaining before update becomes mandatory." else "")

        val notification = NotificationCompat.Builder(context, if (isMandatory) MANDATORY_CHANNEL_ID else UPDATE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_taskit)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(if (isMandatory) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val UPDATE_CHANNEL_ID = "app_updates"
        private const val MANDATORY_CHANNEL_ID = "mandatory_updates"
        private const val UPDATE_NOTIFICATION_ID = 1001
    }
}
