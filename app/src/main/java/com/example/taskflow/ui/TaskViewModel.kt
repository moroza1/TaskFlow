package com.example.taskflow.ui

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskflow.data.AttachmentType
import com.example.taskflow.data.GeminiAgent
import com.example.taskflow.data.Settings
import com.example.taskflow.data.Task
import com.example.taskflow.data.TaskAttachment
import com.example.taskflow.data.TaskCategory
import com.example.taskflow.data.TaskPriority
import com.example.taskflow.data.TaskStatus
import com.example.taskflow.data.TaskUiState
import com.example.taskflow.ui.notifications.TaskReminderReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TaskViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState

    fun addTaskFromVoice(transcript: String) {
        if (transcript.isBlank()) return
        addTask(title = transcript.trim(), details = "Captured via microphone")
        _uiState.update { it.copy(lastCapturedText = transcript.trim()) }
    }

    fun addTask(
        title: String,
        details: String = "",
        priority: TaskPriority = TaskPriority.MEDIUM,
        category: TaskCategory = TaskCategory.WORK,
        dueDate: LocalDate? = null,
        dueTime: LocalTime? = null
    ) {
        val newTask = Task(
            title = title,
            details = details,
            priority = priority,
            category = category,
            dueDate = dueDate,
            dueTime = dueTime
        )
        _uiState.update { state ->
            state.copy(tasks = state.tasks + newTask)
        }
        scheduleReminder(newTask)
    }

    fun updateTask(task: Task) {
        _uiState.update { state ->
            state.copy(tasks = state.tasks.map { if (it.id == task.id) task else it }, selectedTask = task)
        }
        scheduleReminder(task)
    }

    fun deleteTask(task: Task) {
        _uiState.update { state ->
            state.copy(tasks = state.tasks.filterNot { it.id == task.id }, selectedTask = null)
        }
    }

    fun toggleTaskStatus(task: Task) {
        val nextStatus = when (task.status) {
            TaskStatus.OPEN -> TaskStatus.IN_PROGRESS
            TaskStatus.IN_PROGRESS -> TaskStatus.DONE
            TaskStatus.DONE -> TaskStatus.OPEN
        }
        updateTask(task.copy(status = nextStatus))
    }

    fun selectTask(task: Task?) {
        _uiState.update { it.copy(selectedTask = task, showDetail = task != null) }
    }

    fun setApiKey(value: String) {
        _uiState.update { it.copy(settings = it.settings.copy(apiKey = value)) }
    }

    fun refreshAgents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingAgents = true) }
            delay(600)
            val existingAgents = _uiState.value.settings.agents.filterNot { it.isExpired }
            val newAgents = listOf(
                GeminiAgent(id = "helper", name = "Productivity Helper"),
                GeminiAgent(id = "summarizer", name = "Meeting Summarizer")
            )
            _uiState.update {
                it.copy(
                    settings = it.settings.copy(agents = (existingAgents + newAgents).distinctBy(GeminiAgent::id)),
                    isRefreshingAgents = false
                )
            }
        }
    }

    fun attachAudio(task: Task, uri: String) {
        val updated = task.copy().apply {
            attachments.add(TaskAttachment(uri, AttachmentType.AUDIO))
        }
        updateTask(updated)
    }

    fun attachImage(task: Task, uri: String) {
        val updated = task.copy().apply {
            attachments.add(TaskAttachment(uri, AttachmentType.IMAGE))
        }
        updateTask(updated)
    }

    fun analyzeImageAndCreateTask(uri: String) {
        val title = "Insight from image"
        val details = "Analyzed content from $uri"
        addTask(title = title, details = details, category = TaskCategory.HOME, priority = TaskPriority.LOW)
    }

    fun analyzeAudioAndCreateTask() {
        addTask(title = "Voice Task", details = "Created from recorded audio")
    }

    fun updateDueDate(task: Task, date: LocalDate?) {
        updateTask(task.copy(dueDate = date))
    }

    fun updateDueTime(task: Task, time: LocalTime?) {
        updateTask(task.copy(dueTime = time))
    }

    fun setReminder(task: Task, hoursBefore: Int?) {
        updateTask(task.copy(reminderHoursBefore = hoursBefore))
    }

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TaskReminderReceiver.CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun scheduleReminder(task: Task) {
        val context = reminderContext ?: return
        val dueDate = task.dueDate ?: return
        val dueTime = task.dueTime ?: LocalTime.of(9, 0)
        val reminderTime = LocalDateTime.of(dueDate, dueTime)
            .minusHours((task.reminderHoursBefore ?: 1).toLong())
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, task.title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pendingIntent
        )
    }

    private var reminderContext: Context? = null
    fun attachReminderContext(context: Context) {
        reminderContext = context.applicationContext
    }
}
