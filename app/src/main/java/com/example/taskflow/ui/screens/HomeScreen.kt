package com.example.taskflow.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.taskflow.data.Task
import com.example.taskflow.data.TaskCategory
import com.example.taskflow.data.TaskPriority
import com.example.taskflow.data.TaskStatus
import com.example.taskflow.data.TaskUiState
import com.example.taskflow.data.isDue
import com.example.taskflow.ui.TaskViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(uiState: TaskUiState, viewModel: TaskViewModel) {
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val notificationPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)

    LaunchedEffect(notificationPermission.status) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermission.status.isGranted) {
            notificationPermission.launchPermissionRequest()
        }
    }
    val requestMicPermission: (onGranted: () -> Unit) -> Unit = remember(micPermission) {
        { onGranted ->
            if (micPermission.status.isGranted) {
                onGranted()
            } else {
                micPermission.launchPermissionRequest()
            }
        }
    }
    var manualTitle by remember { mutableStateOf("") }
    var manualDetails by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(TaskCategory.WORK) }
    var dueDate by remember { mutableStateOf<LocalDate?>(null) }
    var dueTime by remember { mutableStateOf<LocalTime?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Your day, streamlined") }, actions = {
            IconButton(onClick = { /* sorting placeholder */ }) {
                Icon(Icons.Filled.Sort, contentDescription = "Sort tasks")
            }
        })
        Text(
            text = "Capture tasks fast via mic or manual entry.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Add task manually", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = manualTitle,
                    onValueChange = { manualTitle = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = manualDetails,
                    onValueChange = { manualDetails = it },
                    label = { Text("Details") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrioritySelector(selectedPriority) { selectedPriority = it }
                    CategorySelector(selectedCategory) { selectedCategory = it }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateChip(label = dueDate?.toString() ?: "Due date") { dueDate = LocalDate.now() }
                    DateChip(label = dueTime?.toString() ?: "Due time") { dueTime = LocalTime.of(9, 0) }
                }
                Button(
                    onClick = {
                        viewModel.addTask(
                            title = manualTitle,
                            details = manualDetails,
                            priority = selectedPriority,
                            category = selectedCategory,
                            dueDate = dueDate,
                            dueTime = dueTime
                        )
                        manualTitle = ""
                        manualDetails = ""
                        dueDate = null
                        dueTime = null
                    },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add task")
                }
            }
        }

        Card(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Voice capture", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Tap mic to transcribe and auto-create a task", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = {
                    requestMicPermission { viewModel.analyzeAudioAndCreateTask() }
                }) {
                    Icon(Icons.Filled.Mic, contentDescription = "Record")
                }
            }
        }

        if (uiState.lastCapturedText.isNotBlank()) {
            AssistChip(
                onClick = {},
                label = { Text("Heard: ${uiState.lastCapturedText}") },
                leadingIcon = { Icon(Icons.Filled.Mic, contentDescription = null) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        TaskList(
            tasks = uiState.tasks,
            onToggle = { viewModel.toggleTaskStatus(it) },
            onDelete = { viewModel.deleteTask(it) },
            onOpen = { viewModel.selectTask(it) }
        )

        uiState.selectedTask?.let { task ->
            if (uiState.showDetail) {
                TaskDetailSheet(
                    task,
                    onDismiss = { viewModel.selectTask(null) },
                    requestMicPermission = requestMicPermission,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun PrioritySelector(selected: TaskPriority, onSelected: (TaskPriority) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Priority") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().weight(1f)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskPriority.values().forEach { priority ->
                DropdownMenuItem(text = { Text(priority.name) }, onClick = {
                    onSelected(priority)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun CategorySelector(selected: TaskCategory, onSelected: (TaskCategory) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().weight(1f)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskCategory.values().forEach { category ->
                DropdownMenuItem(text = { Text(category.name) }, onClick = {
                    onSelected(category)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun DateChip(label: String, onClick: () -> Unit) {
    AssistChip(onClick = onClick, label = { Text(label) })
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TaskList(tasks: List<Task>, onToggle: (Task) -> Unit, onDelete: (Task) -> Unit, onOpen: (Task) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tasks, key = { it.id }) { task ->
            SwipeableTaskCard(task = task, onToggle = { onToggle(task) }, onDelete = { onDelete(task) }, onOpen = { onOpen(task) })
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeableTaskCard(task: Task, onToggle: () -> Unit, onDelete: () -> Unit, onOpen: () -> Unit) {
    val swipeableState = rememberSwipeableState(0)
    val anchors = mapOf(0f to 0, -300f to 1)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount.x < -20) onDelete()
                }
            }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (task.isDue()) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface)
        ) {
            ListItem(
                headlineContent = {
                    Text(task.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                },
                supportingContent = {
                    Column {
                        if (task.details.isNotBlank()) {
                            Text(task.details, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Text("${task.category.name} • ${task.priority.name} • ${task.status.name}")
                        task.dueDate?.let { Text("Due ${it}${task.dueTime?.let { time -> " at $time" } ?: ""}") }
                    }
                },
                trailingContent = {
                    Column(horizontalAlignment = Alignment.End) {
                        AssistChip(onClick = onToggle, label = { Text("Next") })
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(onClick = onOpen, label = { Text("Details") })
                    }
                }
            )
        }
    }
}

@Composable
fun TaskDetailSheet(
    task: Task,
    onDismiss: () -> Unit,
    requestMicPermission: (onGranted: () -> Unit) -> Unit,
    viewModel: TaskViewModel
) {
    var title by remember { mutableStateOf(task.title) }
    var details by remember { mutableStateOf(task.details) }
    var priority by remember { mutableStateOf(task.priority) }
    var category by remember { mutableStateOf(task.category) }
    var status by remember { mutableStateOf(task.status) }
    var dueDate by remember { mutableStateOf(task.dueDate) }
    var dueTime by remember { mutableStateOf(task.dueTime) }
    var reminder by remember { mutableStateOf(task.reminderHoursBefore ?: 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                viewModel.updateTask(
                    task.copy(
                        title = title,
                        details = details,
                        priority = priority,
                        category = category,
                        status = status,
                        dueDate = dueDate,
                        dueTime = dueTime,
                        reminderHoursBefore = reminder
                    )
                )
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Task details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = details, onValueChange = { details = it }, label = { Text("Details") }, modifier = Modifier.fillMaxWidth())
                PrioritySelector(priority) { priority = it }
                CategorySelector(category) { category = it }
                StatusSelector(status) { status = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateChip(label = dueDate?.toString() ?: "Due date") { dueDate = LocalDate.now() }
                    DateChip(label = dueTime?.toString() ?: "Due time") { dueTime = LocalTime.of(9, 0) }
                }
                OutlinedTextField(
                    value = reminder.toString(),
                    onValueChange = { reminder = it.toIntOrNull() ?: reminder },
                    label = { Text("Remind hours before") }
                )
                AttachmentActions(
                    onAudio = { requestMicPermission { viewModel.attachAudio(task, "audio://sample") } },
                    onImage = { viewModel.attachImage(task, "image://sample") },
                    onAnalyzeImage = { viewModel.analyzeImageAndCreateTask("image://analysis") },
                    onVoiceDetail = { requestMicPermission { viewModel.addTaskFromVoice("Dictated details for ${task.title}") } }
                )
            }
        }
    )
}

@Composable
private fun StatusSelector(selected: TaskStatus, onSelected: (TaskStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskStatus.values().forEach { status ->
                DropdownMenuItem(text = { Text(status.name) }, onClick = {
                    onSelected(status)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun AttachmentActions(onAudio: () -> Unit, onImage: () -> Unit, onAnalyzeImage: () -> Unit, onVoiceDetail: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Attachments", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = onAudio) {
                Icon(Icons.Filled.Mic, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Attach audio")
            }
            FilledTonalButton(onClick = onImage) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Attach image")
            }
        }
        FilledTonalButton(onClick = onAnalyzeImage, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Analyze image to create task")
        }
        FilledTonalButton(onClick = onVoiceDetail, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Mic, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Dictate more details")
        }
    }
}
