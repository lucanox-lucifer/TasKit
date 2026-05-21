package com.arcanox.taskit.ui.task

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcanox.taskit.data.local.entity.CategoryEntity
import com.arcanox.taskit.data.local.entity.Priority
import com.arcanox.taskit.data.local.entity.TaskEntity
import com.arcanox.taskit.data.repository.TaskRepository
import com.arcanox.taskit.util.ReminderReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0
)

data class CategoryWithCount(
    val category: CategoryEntity,
    val taskCount: Int
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val application: Application
) : ViewModel() {

    private val _categorySearchQuery = MutableStateFlow("")
    val categorySearchQuery: StateFlow<String> = _categorySearchQuery

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allTasksUnfiltered: StateFlow<List<TaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesWithCounts: StateFlow<List<CategoryWithCount>> = combine(
        categories,
        allTasksUnfiltered,
        _categorySearchQuery
    ) { categories, tasks, query ->
        categories
            .filter { it.name.contains(query, ignoreCase = true) }
            .map { category ->
                CategoryWithCount(
                    category = category,
                    taskCount = tasks.count { it.category == category.name }
                )
            }
            .distinctBy { it.category.name } // Extra safety for duplicate names
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Use a slight delay or wait for the first emission to check properly
            val currentCategories = repository.getAllCategories().first()
            if (currentCategories.isEmpty()) {
                repository.prepopulateCategories()
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    val allTasks: StateFlow<List<TaskEntity>> = combine(_searchQuery, _selectedCategory) { query, category ->
        query to category
    }.flatMapLatest { (query, category) ->
        if (query.isEmpty()) {
            if (category == null || category == "All Tasks") {
                repository.getAllTasks()
            } else {
                repository.getTasksByCategory(category)
            }
        } else {
            repository.searchTasks(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String?) {
        _selectedCategory.value = if (category == "All Tasks") null else category
    }

    val activeTasks: StateFlow<List<TaskEntity>> = allTasks
        .map { list -> list.filter { !it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<TaskEntity>> = allTasks
        .map { list -> list.filter { it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<TaskStats> = allTasksUnfiltered.map { list ->
        val completed = list.count { it.isCompleted }
        TaskStats(
            totalTasks = list.size,
            completedTasks = completed
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskStats())

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySearchQueryChange(newQuery: String) {
        _categorySearchQuery.value = newQuery
    }

    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
            val id = repository.insertTask(task)
            if (task.reminderTime != null) {
                scheduleReminder(task.copy(id = id.toInt()))
            }
        }
    }

    private fun scheduleReminder(task: TaskEntity) {
        val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(application, ReminderReceiver::class.java).apply {
            putExtra("TASK_TITLE", task.title)
            putExtra("TASK_NOTE", task.note)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            application,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        task.reminderTime?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, it, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, it, pendingIntent)
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    // Categories
    fun addCategory(name: String, color: Int? = null) {
        viewModelScope.launch {
            // Case-insensitive check for existence
            val current = repository.getAllCategories().first()
            if (current.none { it.name.trim().equals(name.trim(), ignoreCase = true) }) {
                repository.insertCategory(CategoryEntity(name = name.trim(), color = color))
            }
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}
