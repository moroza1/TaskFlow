package com.example.taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.taskflow.ui.TaskFlowApp
import com.example.taskflow.ui.TaskViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.ensureNotificationChannel(this)
        viewModel.attachReminderContext(this)
        setContent {
            TaskFlowApp(viewModel)
        }
    }
}
