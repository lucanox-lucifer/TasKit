package com.arcanox.taskit.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arcanox.taskit.data.local.entity.Priority
import com.arcanox.taskit.data.local.entity.RepeatInterval
import com.arcanox.taskit.data.local.entity.TaskEntity
import com.arcanox.taskit.data.local.entity.CategoryEntity
import com.arcanox.taskit.util.CategoryUtils
import com.arcanox.taskit.ui.theme.*
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditScreen(
    taskId: Int,
    viewModel: TaskViewModel,
    onBack: () -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    val existingTask = remember(taskId, tasks) { tasks.find { it.id == taskId } }

    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var note by remember { mutableStateOf(existingTask?.note ?: "") }
    var category by remember { mutableStateOf(existingTask?.category ?: "Personal") }
    var priority by remember { mutableStateOf(existingTask?.priority ?: Priority.MEDIUM) }
    var repeatInterval by remember { mutableStateOf(existingTask?.repeatInterval ?: RepeatInterval.NONE) }
    
    var dueDate by remember { mutableStateOf(existingTask?.dueDate?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }) }
    var dueTime by remember { mutableStateOf(existingTask?.dueTime?.let { LocalTime.parse(it) }) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            confirmButton = {
                TextButton(onClick = {
                    dueDate = datePickerState.selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = dueTime?.hour ?: LocalTime.now().hour,
            initialMinute = dueTime?.minute ?: LocalTime.now().minute
        )
        BasicAlertDialog(
            onDismissRequest = { showTimePicker = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            content = {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TimePicker(state = timePickerState)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showTimePicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            TextButton(onClick = {
                                dueTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                showTimePicker = false
                            }) { Text("OK", color = MaterialTheme.colorScheme.primary) }
                        }
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == -1) "New Task" else "Edit Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                // Calculate reminderTime based on dueDate and dueTime
                                val reminderMillis = if (dueDate != null && dueTime != null) {
                                    LocalDateTime.of(dueDate, dueTime)
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant()
                                        .toEpochMilli()
                                } else {
                                    null
                                }

                                if (taskId == -1) {
                                    viewModel.addTask(
                                        TaskEntity(
                                            title = title,
                                            note = note.ifBlank { null },
                                            category = category,
                                            priority = priority,
                                            repeatInterval = repeatInterval,
                                            dueDate = dueDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                                            dueTime = dueTime?.format(DateTimeFormatter.ISO_LOCAL_TIME),
                                            reminderTime = reminderMillis
                                        )
                                    )
                                } else {
                                    existingTask?.let {
                                        viewModel.updateTask(
                                            it.copy(
                                                title = title,
                                                note = note.ifBlank { null },
                                                category = category,
                                                priority = priority,
                                                repeatInterval = repeatInterval,
                                                dueDate = dueDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                                                dueTime = dueTime?.format(DateTimeFormatter.ISO_LOCAL_TIME),
                                                reminderTime = reminderMillis
                                            )
                                        )
                                    }
                                }
                                onBack()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = if (title.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Title Field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TITLE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("What needs to be done?", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // Description Field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DESCRIPTION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                TextField(
                    value = note ?: "",
                    onValueChange = { note = it },
                    placeholder = { Text("Add more details...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // Category & Priority Section
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("CATEGORY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    CategoryDropdown(
                        selected = category,
                        categories = categories,
                        onSelect = { category = it }
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PRIORITY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    PriorityDropdown(
                        selected = priority,
                        onSelect = { priority = it }
                    )
                }
            }

            // Date & Time Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DUE DATE & TIME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(dueDate?.format(DateTimeFormatter.ofPattern("MMM dd")) ?: "Set Date", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Surface(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(dueTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "Set Time", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Repeat Interval
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("REPEAT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RepeatInterval.entries.forEach { interval ->
                        FilterChip(
                            selected = repeatInterval == interval,
                            onClick = { repeatInterval = interval },
                            label = { Text(interval.name) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = repeatInterval == interval,
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp
                            )
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun CategoryDropdown(selected: String, categories: List<CategoryEntity>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.name == selected }
    
    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedCategory != null) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CategoryUtils.getColor(selectedCategory.color))
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(selected, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            if (categories.isEmpty()) {
                listOf("Work", "Personal", "Study", "Life").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = { onSelect(option); expanded = false }
                    )
                }
            } else {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(CategoryUtils.getColor(cat.color))
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(cat.name, color = MaterialTheme.colorScheme.onSurface)
                            }
                        },
                        onClick = { onSelect(cat.name); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityDropdown(selected: Priority, onSelect: (Priority) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selected.name, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            Priority.entries.forEach { prio ->
                DropdownMenuItem(
                    text = { Text(prio.name, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = { onSelect(prio); expanded = false }
                )
            }
        }
    }
}
