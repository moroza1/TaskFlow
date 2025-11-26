package com.example.taskflow.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.taskflow.ui.screens.HomeScreen
import com.example.taskflow.ui.screens.SettingsScreen
import com.example.taskflow.ui.theme.TaskFlowTheme

sealed class TaskFlowDestination(val route: String, val label: String, val icon: ImageVector) {
    data object Home : TaskFlowDestination("home", "Tasks", Icons.Filled.Home)
    data object Settings : TaskFlowDestination("settings", "Settings", Icons.Filled.Settings)
}

@Composable
fun TaskFlowApp(viewModel: TaskViewModel) {
    TaskFlowTheme {
        val navController = rememberNavController()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination?.route
                    val destinations = listOf(TaskFlowDestination.Home, TaskFlowDestination.Settings)
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = { Text(destination.label) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = TaskFlowDestination.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(TaskFlowDestination.Home.route) {
                    HomeScreen(uiState, viewModel)
                }
                composable(TaskFlowDestination.Settings.route) {
                    SettingsScreen(uiState, viewModel)
                }
            }
        }
    }
}
