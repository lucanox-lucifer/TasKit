package com.arcanox.taskit.data.repository

import com.arcanox.taskit.data.local.dao.CategoryDao
import com.arcanox.taskit.data.local.dao.TaskDao
import com.arcanox.taskit.data.local.entity.CategoryEntity
import com.arcanox.taskit.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
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
    fun getTasksByCategory(category: String): Flow<List<TaskEntity>> = taskDao.getTasksByCategory(category)

    // Categories
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories().distinctUntilChanged()
    suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    suspend fun prepopulateCategories() {
        val defaults = listOf(
            CategoryEntity(name = "Work", icon = "WorkOutline", color = 0xFF8ED081.toInt()),
            CategoryEntity(name = "Personal", icon = "PersonOutline", color = 0xFFFA6B6B.toInt()),
            CategoryEntity(name = "Study", icon = "Book", color = 0xFF9E87F5.toInt()),
            CategoryEntity(name = "Shopping", icon = "ShoppingCart", color = 0xFFF5C451.toInt()),
            CategoryEntity(name = "Fitness", icon = "FitnessCenter", color = 0xFF8ED081.toInt()),
            CategoryEntity(name = "Ideas", icon = "Lightbulb", color = 0xFF9B6B43.toInt())
        )
        defaults.forEach { insertCategory(it) }
    }
}
