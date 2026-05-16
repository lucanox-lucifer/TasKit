package com.arcanox.taskit.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val note = intent.getStringExtra("TASK_NOTE") ?: "You have a task due now!"
        
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, note)
    }
}
