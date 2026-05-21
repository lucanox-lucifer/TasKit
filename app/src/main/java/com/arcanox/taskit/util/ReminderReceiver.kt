package com.arcanox.taskit.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    
    @Inject lateinit var notificationHelper: NotificationHelper
    
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val note = intent.getStringExtra("TASK_NOTE") ?: "You have a task due now!"
        
        notificationHelper.showNotification(title, note)
    }
}
