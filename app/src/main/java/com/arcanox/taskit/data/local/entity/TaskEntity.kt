package com.arcanox.taskit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority {
    LOW, MEDIUM, HIGH
}

enum class RepeatInterval {
    NONE, DAILY, WEEKLY, MONTHLY
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val note: String? = null,
    val category: String,
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val reminderTime: Long? = null,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
