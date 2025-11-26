package com.example.taskflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.taskflow.data.TaskUiState
import com.example.taskflow.ui.TaskViewModel

@Composable
fun SettingsScreen(uiState: TaskUiState, viewModel: TaskViewModel) {
    var apiKey by remember(uiState.settings.apiKey) { mutableStateOf(uiState.settings.apiKey) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Gemini integration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = {
                        apiKey = it
                        viewModel.setApiKey(it)
                    },
                    label = { Text("API key") },
                    leadingIcon = { Icon(Icons.Outlined.Api, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Get your Gemini API key",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "https://aistudio.google.com/app/apikey",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Available Gemini agents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.refreshAgents() }, enabled = !uiState.isRefreshingAgents) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh agents")
                    }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(uiState.settings.agents) { agent ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(agent.name, fontWeight = FontWeight.SemiBold)
                                    Text(agent.id, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Text(if (agent.isExpired) "Expired" else "Active", color = if (agent.isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        Divider()
        Text("Version 1.1", style = MaterialTheme.typography.bodyMedium)
        Text("© Developed by Mohamed Roshdy EL Zahar", style = MaterialTheme.typography.bodyMedium)
    }
}
