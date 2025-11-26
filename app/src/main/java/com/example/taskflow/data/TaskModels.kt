package com.example.taskflow.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var details: String = "",
    var priority: TaskPriority = TaskPriority.MEDIUM,
    var category: TaskCategory = TaskCategory.WORK,
    var status: TaskStatus = TaskStatus.OPEN,
    var dueDate: LocalDate? = null,
    var dueTime: LocalTime? = null,
    var reminderHoursBefore: Int? = null,
    val attachments: MutableList<TaskAttachment> = mutableListOf()
)

data class TaskAttachment(
    val uri: String,
    val type: AttachmentType
)

enum class AttachmentType { AUDIO, IMAGE }

enum class TaskPriority { LOW, MEDIUM, HIGH }

enum class TaskCategory { WORK, PRIVATE, HOME }

enum class TaskStatus { OPEN, IN_PROGRESS, DONE }

data class GeminiAgent(val id: String, val name: String, val isExpired: Boolean = false)

data class Settings(
    val apiKey: String = "",
    val agents: List<GeminiAgent> = emptyList()
)

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val settings: Settings = Settings(),
    val selectedTask: Task? = null,
    val showDetail: Boolean = false,
    val microphoneEnabled: Boolean = false,
    val lastCapturedText: String = "",
    val isRefreshingAgents: Boolean = false
)

fun Task.isDue(now: LocalDateTime = LocalDateTime.now()): Boolean {
    val date = dueDate ?: return false
    val time = dueTime ?: LocalTime.of(9, 0)
    val dueDateTime = LocalDateTime.of(date, time)
    return now.isAfter(dueDateTime)
}
