package com.example.poultryfarmmanager.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poultryfarmmanager.data.repository.FirestoreRepository
import com.example.poultryfarmmanager.data.repository.LocalRepository
import com.example.poultryfarmmanager.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val localRepository: LocalRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask = _editingTask.asStateFlow()

    init {
        viewModelScope.launch {
            localRepository.observeTasks().collect { _tasks.value = it }
        }
    }

    fun showAddDialog() { _editingTask.value = null; _showAddDialog.value = true }
    fun showEditDialog(t: Task) { _editingTask.value = t; _showAddDialog.value = true }
    fun hideDialog() { _showAddDialog.value = false; _editingTask.value = null }

    fun addTask(title: String, desc: String, dueDate: Long) {
        viewModelScope.launch {
            val task = Task(title = title, description = desc, dueDate = dueDate)
            localRepository.addTask(task)
            hideDialog()
            try { firestoreRepository.saveTask(task) } catch (_: Exception) {}
        }
    }

    fun updateTask(t: Task, title: String, desc: String, dueDate: Long) {
        viewModelScope.launch {
            val updated = t.copy(title = title, description = desc, dueDate = dueDate)
            localRepository.updateTask(updated)
            hideDialog()
            try { firestoreRepository.saveTask(updated) } catch (_: Exception) {}
        }
    }

    fun toggleTask(t: Task) {
        viewModelScope.launch {
            val updated = t.copy(isCompleted = !t.isCompleted)
            localRepository.updateTask(updated)
            try { firestoreRepository.saveTask(updated) } catch (_: Exception) {}
        }
    }

    fun deleteTask(t: Task) {
        viewModelScope.launch {
            localRepository.deleteTask(t)
            try { firestoreRepository.deleteTask(t.id) } catch (_: Exception) {}
        }
    }
}
