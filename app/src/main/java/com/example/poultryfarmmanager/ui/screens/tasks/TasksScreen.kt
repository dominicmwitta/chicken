package com.example.poultryfarmmanager.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.poultryfarmmanager.domain.model.Task
import com.example.poultryfarmmanager.ui.components.EmptyStateMessage
import com.example.poultryfarmmanager.ui.components.KukuCard
import com.example.poultryfarmmanager.ui.theme.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(showAdd: Boolean = false, viewModel: TasksViewModel = hiltViewModel()) {
    val s = LocalAppStrings.current
    LaunchedEffect(showAdd) { if (showAdd) viewModel.showAddDialog() }
    val tasks         by viewModel.tasks.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val editingTask   by viewModel.editingTask.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(s.addTaskLabel, fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { paddingValues ->
        if (tasks.isEmpty()) {
            EmptyStateMessage(s.noTasks, modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(s.tasksTitle, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 4.dp))
                }
                items(tasks, key = { it.id }) { task ->
                    TaskCard(task = task, onToggle = { viewModel.toggleTask(task) },
                        onEdit = { viewModel.showEditDialog(task) }, onDelete = { viewModel.deleteTask(task) })
                }
            }
        }

        if (showAddDialog) {
            AddEditTaskDialog(
                task = editingTask,
                onDismiss = { viewModel.hideDialog() },
                onSave = { title, description, dueDate ->
                    if (editingTask != null) viewModel.updateTask(editingTask!!, title, description, dueDate)
                    else viewModel.addTask(title, description, dueDate)
                }
            )
        }
    }
}

@Composable
private fun TaskCard(task: Task, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val s = LocalAppStrings.current
    val isOverdue = !task.isCompleted && task.dueDate < System.currentTimeMillis()
    val accentColor = when {
        task.isCompleted -> MaterialTheme.colorScheme.outline
        isOverdue        -> MaterialTheme.colorScheme.error
        else             -> MaterialTheme.colorScheme.primary
    }

    KukuCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = when {
            task.isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            isOverdue        -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
            else             -> MaterialTheme.colorScheme.surface
        },
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp).height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Spacer(Modifier.width(2.dp))

            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface
                )
                if (task.description.isNotEmpty()) {
                    Text(task.description, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = if (isOverdue) "${s.overdue}${formatDate(task.dueDate)}"
                           else "${s.due}${formatDate(task.dueDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isOverdue) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditTaskDialog(task: Task?, onDismiss: () -> Unit, onSave: (String, String, Long) -> Unit) {
    val s = LocalAppStrings.current
    var title          by remember { mutableStateOf(task?.title ?: "") }
    var description    by remember { mutableStateOf(task?.description ?: "") }
    var dueDate        by remember { mutableStateOf(task?.dueDate ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(if (task != null) s.editTask else s.addTaskTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text(s.taskTitle) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text(s.descriptionOptional) }, modifier = Modifier.fillMaxWidth(),
                    minLines = 2, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(
                    value = formatDate(dueDate), onValueChange = {}, label = { Text(s.dueDate) },
                    readOnly = true, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, null)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onSave(title, description, dueDate) },
                enabled = title.isNotBlank(), shape = RoundedCornerShape(10.dp)
            ) { Text(s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(s.cancel) } }
        ) { DatePicker(state = datePickerState) }
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
