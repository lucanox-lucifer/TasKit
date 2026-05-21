package com.arcanox.taskit.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.arcanox.taskit.MainActivity
import com.arcanox.taskit.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val UPDATE_CHANNEL_ID = "taskit_updates"
        private const val REMINDER_CHANNEL_ID = "taskit_reminders"
        private const val UPDATE_NOTIFICATION_ID = 1001
        private const val REMINDER_NOTIFICATION_ID = 2001
    }

    init {
        createNotificationChannels()
    }

    fun createNotificationChannels() {
        val updateChannel = NotificationChannel(
            UPDATE_CHANNEL_ID, 
            "App Updates", 
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for TasKit app updates"
        }
        
        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for your tasks"
        }
        
        notificationManager.createNotificationChannel(updateChannel)
        notificationManager.createNotificationChannel(reminderChannel)
    }

    fun showUpdateNotification(versionName: String, changelog: String, isMandatory: Boolean = false, daysLeft: Int? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = if (isMandatory && daysLeft != null) {
            "Update required in $daysLeft days. v$versionName"
        } else {
            changelog.take(100) + "..."
        }

        val notification = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setContentTitle(if (isMandatory) "Critical Update Available" else "New Update Available: $versionName")
            .setContentText(message)
            .setPriority(if (isMandatory) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
    }

    fun showNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(REMINDER_NOTIFICATION_ID, notification)
    }
}
