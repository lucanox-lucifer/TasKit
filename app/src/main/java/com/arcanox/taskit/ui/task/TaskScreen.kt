package com.arcanox.taskit.ui.task

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.arcanox.taskit.data.local.entity.Priority
import com.arcanox.taskit.data.local.entity.TaskEntity
import com.arcanox.taskit.data.local.entity.CategoryEntity
import com.arcanox.taskit.util.CategoryUtils
import com.arcanox.taskit.ui.theme.*
import kotlinx.coroutines.delay

@Preview
@Composable
fun TaskScreenPreview() {
    TasKitTheme {
        TaskScreenContent(
            activeTasks = listOf(
                TaskEntity(id = 1, title = "Buy groceries", note = "Milk, Eggs, Bread", category = "Personal", priority = Priority.HIGH),
                TaskEntity(id = 2, title = "Workout for 30 minutes", note = "Morning cardio", category = "Fitness", priority = Priority.MEDIUM)
            ),
            completedTasks = listOf(
                TaskEntity(id = 3, title = "Complete UI redesign", isCompleted = true, category = "Work", priority = Priority.HIGH)
            ),
            stats = TaskStats(totalTasks = 12, completedTasks = 5),
            selectedCategory = null,
            categories = listOf(
                CategoryEntity(name = "Personal", color = 0xFFFA6B6B.toInt()),
                CategoryEntity(name = "Work", color = 0xFF8ED081.toInt()),
                CategoryEntity(name = "Fitness", color = 0xFF8ED081.toInt())
            ),
            searchQuery = "",
            isTablet = false,
            onToggleCompletion = {},
            onDelete = {},
            onEditTask = {},
            onOpenDrawer = {},
            onSearchQueryChange = {}
        )
    }
}

@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    isTablet: Boolean,
    onEditTask: (Int) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val activeTasks by viewModel.activeTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    TaskScreenContent(
        activeTasks = activeTasks,
        completedTasks = completedTasks,
        stats = stats,
        selectedCategory = selectedCategory,
        categories = categories,
        searchQuery = searchQuery,
        isTablet = isTablet,
        onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
        onDelete = { viewModel.deleteTask(it) },
        onEditTask = onEditTask,
        onOpenDrawer = onOpenDrawer,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreenContent(
    activeTasks: List<TaskEntity>,
    completedTasks: List<TaskEntity>,
    stats: TaskStats,
    selectedCategory: String?,
    categories: List<CategoryEntity>,
    searchQuery: String,
    isTablet: Boolean,
    onToggleCompletion: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    onEditTask: (Int) -> Unit,
    onOpenDrawer: () -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
            }
            
            if (isSearchExpanded) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    placeholder = { Text("Search tasks...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    ),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { 
                            onSearchQueryChange("")
                            isSearchExpanded = false 
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            } else {
                Text(
                    "TasKit",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
                IconButton(onClick = { isSearchExpanded = true }) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
            
            IconButton(onClick = { /* Notifications */ }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isTablet) 32.dp else 0.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth()) {
                    HeaderSection(selectedCategory)
                }
            }

            item {
                Box(modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth()) {
                    StatsSection(stats)
                }
            }

            item {
                Box(modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth()) {
                    SectionHeader("Tasks", onSeeAll = {})
                }
            }

            if (activeTasks.isEmpty() && completedTasks.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(activeTasks, key = { it.id }) { task ->
                    Box(modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth()) {
                        TaskCard(
                            task = task,
                            category = categories.find { it.name == task.category },
                            onToggle = { onToggleCompletion(it) },
                            onDelete = { onDelete(it) },
                            onClick = { onEditTask(task.id) }
                        )
                    }
                }

                if (completedTasks.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth()) {
                            SectionHeader("Completed", onSeeAll = null)
                        }
                    }
                    items(completedTasks, key = { it.id }) { task ->
                        Box(modifier = Modifier.widthIn(max = 800.dp).fillMaxWidth()) {
                            TaskCard(
                                task = task,
                                category = categories.find { it.name == task.category },
                                onToggle = { onToggleCompletion(it) },
                                onDelete = { onDelete(it) },
                                onClick = { onEditTask(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(selectedCategory: String?) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text("Hello, Guest 👋", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            selectedCategory ?: "Manage Your Tasks",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatsSection(stats: TaskStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Total",
            value = stats.totalTasks.toString(),
            icon = Icons.Default.Description,
            modifier = Modifier.weight(1f),
            iconColor = MaterialTheme.colorScheme.primary
        )
        StatCard(
            title = "Pending",
            value = (stats.totalTasks - stats.completedTasks).toString(),
            icon = Icons.Default.Schedule,
            modifier = Modifier.weight(1f),
            iconColor = WarningYellow
        )
        StatCard(
            title = "Done",
            value = stats.completedTasks.toString(),
            icon = Icons.Default.CheckCircle,
            modifier = Modifier.weight(1f),
            iconColor = SuccessGreen
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    iconColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        if (onSeeAll != null) {
            Text(
                "See All",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskCard(
    task: TaskEntity,
    category: CategoryEntity?,
    onToggle: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    onClick: () -> Unit
) {
    val priorityColor = when (task.priority) {
        Priority.LOW -> SuccessGreen
        Priority.MEDIUM -> WarningYellow
        Priority.HIGH -> ErrorRed
    }

    val categoryColor = CategoryUtils.getColor(category?.color)

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(priorityColor)
                )
                
                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .border(
                            width = if (task.isCompleted) 0.dp else 1.5.dp,
                            color = if (task.isCompleted) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onToggle(task) },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            task.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                            ),
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (category != null) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                category.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                    if (!task.note.isNullOrEmpty()) {
                        Text(
                            task.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                IconButton(onClick = onClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }

                IconButton(onClick = { onDelete(task) }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
        Spacer(Modifier.height(16.dp))
        Text("No tasks yet ✨", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Text("Focus on your futuristic goals.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}
