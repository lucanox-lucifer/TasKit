package com.arcanox.taskit.data.repository

import com.arcanox.taskit.data.local.dao.CategoryDao
import com.arcanox.taskit.data.local.dao.TaskDao
import com.arcanox.taskit.data.local.entity.CategoryEntity
import com.arcanox.taskit.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao
) {
    // Tasks
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    suspend fun getTaskById(id: Int): TaskEntity? = taskDao.getTaskById(id)
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    fun searchTasks(query: String): Flow<List<TaskEntity>> = taskDao.searchTasks(query)

    // Categories
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)
}
