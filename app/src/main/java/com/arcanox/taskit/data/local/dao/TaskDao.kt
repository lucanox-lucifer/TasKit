package com.arcanox.taskit.data.local.dao

import androidx.room.*
import com.arcanox.taskit.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%'")
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY createdAt DESC")
    fun getTasksByCategory(category: String): Flow<List<TaskEntity>>
}
