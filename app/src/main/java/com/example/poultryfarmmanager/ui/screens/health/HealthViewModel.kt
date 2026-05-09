package com.example.poultryfarmmanager.ui.screens.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poultryfarmmanager.data.repository.FirestoreRepository
import com.example.poultryfarmmanager.data.repository.LocalRepository
import com.example.poultryfarmmanager.domain.model.HealthRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val localRepository: LocalRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _records = MutableStateFlow<List<HealthRecord>>(emptyList())
    val records: StateFlow<List<HealthRecord>> = _records.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    private val _editingRecord = MutableStateFlow<HealthRecord?>(null)
    val editingRecord = _editingRecord.asStateFlow()

    init {
        viewModelScope.launch {
            localRepository.observeHealth().collect { _records.value = it }
        }
    }

    fun showAddDialog() { _editingRecord.value = null; _showAddDialog.value = true }
    fun showEditDialog(r: HealthRecord) { _editingRecord.value = r; _showAddDialog.value = true }
    fun hideDialog() { _showAddDialog.value = false; _editingRecord.value = null }

    fun addRecord(type: String, name: String, date: Long, cost: Double, dosage: String, birdsAffected: Int, notes: String) {
        viewModelScope.launch {
            val record = HealthRecord(type = type, name = name, date = date, cost = cost,
                dosage = dosage, birdsAffected = birdsAffected, notes = notes)
            localRepository.addHealth(record)
            hideDialog()
            try { firestoreRepository.saveHealth(record) } catch (_: Exception) {}
        }
    }

    fun updateRecord(r: HealthRecord, type: String, name: String, date: Long, cost: Double, dosage: String, birdsAffected: Int, notes: String) {
        viewModelScope.launch {
            val updated = r.copy(type = type, name = name, date = date, cost = cost,
                dosage = dosage, birdsAffected = birdsAffected, notes = notes)
            localRepository.updateHealth(updated)
            hideDialog()
            try { firestoreRepository.saveHealth(updated) } catch (_: Exception) {}
        }
    }

    fun deleteRecord(r: HealthRecord) {
        viewModelScope.launch {
            localRepository.deleteHealth(r)
            try { firestoreRepository.deleteHealth(r.id) } catch (_: Exception) {}
        }
    }
}
