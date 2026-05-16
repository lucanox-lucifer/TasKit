package com.arcanox.taskit.ui.task

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcanox.taskit.data.local.entity.CategoryEntity
import com.arcanox.taskit.data.local.entity.Priority
import com.arcanox.taskit.data.local.entity.TaskEntity
import com.arcanox.taskit.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (Int) -> Unit
) {
    var currentTab by remember { mutableStateOf(0) }
    val activeTasks by viewModel.activeTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showHelpDialog by remember { mutableStateOf(false) }

    // Local settings state
    var notificationsEnabled by remember { mutableStateOf(true) }
    var swipeToDeleteEnabled by remember { mutableStateOf(true) }

    val tabs = listOf(
        TabItem("Home", Icons.Default.Home),
        TabItem("Stats", Icons.Default.Info),
        TabItem("Settings", Icons.Default.Settings)
    )

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "TASKIT",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge,
                            letterSpacing = 4.sp,
                            color = StitchPrimary
                        )
                    },
                    actions = {
                        IconButton(onClick = { showHelpDialog = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Help")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
                if (currentTab == 0) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .height(72.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    tab.icon, 
                                    contentDescription = tab.title,
                                    tint = if (currentTab == index) StitchPrimary else StitchOnSurfaceVariant
                                ) 
                            },
                            label = { 
                                Text(
                                    tab.title, 
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (currentTab == index) StitchPrimary else StitchOnSurfaceVariant
                                ) 
                            },
                            selected = currentTab == index,
                            onClick = { currentTab = index },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = StitchPrimary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentTab == 0) {
                FloatingActionButton(
                    onClick = onAddTask,
                    containerColor = StitchPrimary,
                    contentColor = StitchOnPrimary,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(bottom = 100.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { paddingValues ->
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = { Text("How to use TasKit") },
                text = {
                    Column {
                        Text("• Tap a task's checkbox to complete it.")
                        Text("• Long press an active task to edit its details.")
                        Text("• Swipe a task left to delete it (if enabled).")
                        Text("• Swipe a task right to toggle completion.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) { Text("Got it") }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Crossfade(targetState = currentTab, label = "TabChange") { tabIndex ->
                when (tabIndex) {
                    0 -> TaskList(
                        activeTasks = activeTasks,
                        completedTasks = completedTasks,
                        onToggleCompletion = viewModel::toggleTaskCompletion,
                        onDelete = viewModel::deleteTask,
                        onEdit = onEditTask,
                        swipeEnabled = swipeToDeleteEnabled
                    )
                    1 -> DashboardScreen(stats)
                    2 -> SettingsScreen(
                        isDarkMode = isDarkMode,
                        onDarkModeChange = onDarkModeChange,
                        notificationsEnabled = notificationsEnabled,
                        onNotificationsChange = { notificationsEnabled = it },
                        swipeToDeleteEnabled = swipeToDeleteEnabled,
                        onSwipeToDeleteChange = { swipeToDeleteEnabled = it },
                        categories = categories,
                        onAddCategory = { name, color -> viewModel.addCategory(name, color) },
                        onDeleteCategory = { viewModel.deleteCategory(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search tasks...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskList(
    activeTasks: List<TaskEntity>,
    completedTasks: List<TaskEntity>,
    onToggleCompletion: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    onEdit: (Int) -> Unit,
    swipeEnabled: Boolean = true
) {
    if (activeTasks.isEmpty() && completedTasks.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (activeTasks.isNotEmpty()) {
                item {
                    Text("ACTIVE", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
                items(activeTasks, key = { it.id }) { task ->
                    TaskItemWrapper(task, onToggleCompletion, onDelete, onEdit, swipeEnabled)
                }
            }
            
            if (completedTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("COMPLETED", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
                items(completedTasks, key = { it.id }) { task ->
                    TaskItemWrapper(task, onToggleCompletion, onDelete, onEdit, swipeEnabled)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.TaskItemWrapper(
    task: TaskEntity,
    onToggleCompletion: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    onEdit: (Int) -> Unit,
    swipeEnabled: Boolean
) {
    if (swipeEnabled) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = {
                when (it) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onToggleCompletion(task)
                        false
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        onDelete(task)
                        true
                    }
                    else -> false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF43C478).copy(alpha = 0.2f)
                    SwipeToDismissBoxValue.EndToStart -> StitchError.copy(alpha = 0.2f)
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(color)
                        .padding(horizontal = 24.dp),
                    contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                        Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                            Icons.Default.Check else Icons.Default.Delete,
                        contentDescription = null,
                        tint = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                            Color(0xFF43C478) else StitchError
                    )
                }
            },
            modifier = Modifier.animateItemPlacement()
        ) {
            TaskItem(
                task = task,
                onToggleCompletion = { onToggleCompletion(task) },
                onDelete = { onDelete(task) },
                onLongClick = { onEdit(task.id) }
            )
        }
    } else {
        TaskItem(
            task = task,
            onToggleCompletion = { onToggleCompletion(task) },
            onDelete = { onDelete(task) },
            onLongClick = { onEdit(task.id) },
            modifier = Modifier.animateItemPlacement()
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: TaskEntity,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* No-op, use internal checkbox/buttons */ },
                onLongClick = { onLongClick() }
            )
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) 
                            Brush.linearGradient(listOf(StitchPrimary, StitchSecondary))
                        else 
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .then(if (!task.isCompleted) Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)) else Modifier)
                    .clickable { onToggleCompletion() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = StitchOnPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    ),
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) 
                            else MaterialTheme.colorScheme.onSurface
                )
                if (!task.note.isNullOrEmpty()) {
                    Text(
                        text = task.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StitchChip(task.category, StitchSecondary)
                    StitchPriorityChip(task.priority)
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = "Delete",
                    tint = StitchError.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun StitchChip(text: String, accent: Color) {
    Surface(
        color = accent.copy(alpha = 0.1f),
        shape = CircleShape,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StitchPriorityChip(priority: Priority) {
    val color = when (priority) {
        Priority.LOW -> Color(0xFF43C478)
        Priority.MEDIUM -> StitchTertiary
        Priority.HIGH -> StitchError
    }
    StitchChip(priority.name, color)
}

@Composable
fun DashboardScreen(stats: TaskStats) {
    val progress = if (stats.totalTasks > 0) stats.completedTasks.toFloat() / stats.totalTasks else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "Progress")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Text(
                        "ACTIVITY PROGRESS", 
                        style = MaterialTheme.typography.labelMedium, 
                        fontWeight = FontWeight.ExtraBold,
                        color = StitchPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .background(Brush.linearGradient(listOf(StitchPrimary, StitchSecondary)))
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "${(progress * 100).toInt()}% COMPLETED",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        item {
            StitchStatCard("TASKS COMPLETED", "${stats.completedTasks} / ${stats.totalTasks}", Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun StitchStatCard(title: String, value: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = StitchPrimary)
        }
    }
}

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    swipeToDeleteEnabled: Boolean,
    onSwipeToDeleteChange: (Boolean) -> Unit,
    categories: List<CategoryEntity>,
    onAddCategory: (String, Int?) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    if (showAddCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf(StitchPrimary.toArgb()) }
        
        val colors = listOf(
            StitchPrimary, StitchSecondary, StitchTertiary,
            Color(0xFF43C478), Color(0xFFFF5252), Color(0xFFFF4081),
            Color(0xFF7C4DFF), Color(0xFF536DFE), Color(0xFF00B0FF)
        )

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    Text("Choose Color", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedColor == color.toArgb()) 2.dp else 0.dp,
                                        color = if (selectedColor == color.toArgb()) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = color.toArgb() }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newCatName.isNotBlank()) {
                        onAddCategory(newCatName, selectedColor)
                        showAddCategoryDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("SETTINGS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = StitchPrimary)
        }
        item {
            SettingsItem("Dark Mode", isDarkMode, onDarkModeChange)
        }
        item {
            SettingsItem("Notifications", notificationsEnabled, onNotificationsChange)
        }
        item {
            SettingsItem("Swipe to Delete", swipeToDeleteEnabled, onSwipeToDeleteChange)
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("CATEGORIES", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = StitchPrimary)
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat.name, style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = { onDeleteCategory(cat) }) { 
                                Icon(Icons.Default.Delete, null, tint = StitchError, modifier = Modifier.size(18.dp)) 
                            }
                        }
                    }
                    Button(
                        onClick = { showAddCategoryDialog = true }, 
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StitchPrimary.copy(alpha = 0.1f), 
                            contentColor = StitchPrimary
                        )
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("ADD CATEGORY")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("CHANGELOG", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = StitchPrimary)
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Coming Soon!", fontWeight = FontWeight.Bold)
                    Text("We're working on new features like cloud sync and collaboration.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("ABOUT", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = StitchPrimary)
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF151B2E)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = StitchPrimary
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Text("TasKit", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(24.dp))
                    AboutRow("Publisher", "Arcanox")
                    AboutRow("Managed by", "Arcanox")
                    AboutRow("Powered by", "Lucanox")
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                    AboutRow("Version", "1.0.0 (Build 2026)")
                }
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun SettingsItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Switch(
                checked = checked, 
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = StitchPrimary,
                    checkedTrackColor = StitchPrimary.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(32.dp),
            color = StitchPrimary.copy(alpha = 0.05f)
        ) {
            Icon(
                imageVector = Icons.Default.Checklist,
                contentDescription = null, 
                modifier = Modifier.size(80.dp).padding(16.dp),
                tint = StitchPrimary.copy(alpha = 0.3f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "NO TASKS TODAY ✨", 
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Enjoy your futuristic freedom.", 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class TabItem(val title: String, val icon: ImageVector)
