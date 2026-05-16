package com.arcanox.taskit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arcanox.taskit.data.local.dao.CategoryDao
import com.arcanox.taskit.data.local.dao.TaskDao
import com.arcanox.taskit.data.local.entity.CategoryEntity
import com.arcanox.taskit.data.local.entity.TaskEntity

@Database(entities = [TaskEntity::class, CategoryEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TasKitDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
}
