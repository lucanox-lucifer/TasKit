package com.arcanox.taskit.data.local

import androidx.room.TypeConverter
import com.arcanox.taskit.data.local.entity.Priority
import com.arcanox.taskit.data.local.entity.RepeatInterval

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }

    @TypeConverter
    fun fromRepeatInterval(repeatInterval: RepeatInterval): String {
        return repeatInterval.name
    }

    @TypeConverter
    fun toRepeatInterval(repeatInterval: String): RepeatInterval {
        return RepeatInterval.valueOf(repeatInterval)
    }
}
