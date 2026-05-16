package com.arcanox.taskit.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcanox.taskit.data.local.entity.Priority
import com.arcanox.taskit.data.local.entity.RepeatInterval
import com.arcanox.taskit.data.local.entity.TaskEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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
    
    val existingTask = remember(taskId, tasks) { 
        tasks.find { it.id == taskId } 
    }

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
            confirmButton = {
                TextButton(onClick = {
                    dueDate = datePickerState.selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
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
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == -1) "New Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                if (taskId == -1) {
                                    viewModel.addTask(
                                        TaskEntity(
                                            title = title,
                                            note = note.ifBlank { null },
                                            category = category,
                                            priority = priority,
                                            repeatInterval = repeatInterval,
                                            dueDate = dueDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                                            dueTime = dueTime?.format(DateTimeFormatter.ISO_LOCAL_TIME)
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
                                                dueTime = dueTime?.format(DateTimeFormatter.ISO_LOCAL_TIME)
                                            )
                                        )
                                    }
                                }
                                onBack()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Category", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val catList = if (categories.isEmpty()) listOf("Work", "Personal", "Study", "Life") else categories.map { it.name }
                catList.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Text("Priority", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Priority.entries.forEach { prio ->
                    FilterChip(
                        selected = priority == prio,
                        onClick = { priority = prio },
                        label = { Text(prio.name) }
                    )
                }
            }

            Text("Date & Time", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(dueDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "Set Date")
                    }
                }

                OutlinedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(dueTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "Set Time")
                    }
                }
            }

            Text("Repeat", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RepeatInterval.entries.forEach { interval ->
                    FilterChip(
                        selected = repeatInterval == interval,
                        onClick = { repeatInterval = interval },
                        label = { Text(interval.name) }
                    )
                }
            }
        }
    }
}
