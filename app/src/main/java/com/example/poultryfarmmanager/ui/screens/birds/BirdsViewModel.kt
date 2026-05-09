package com.example.poultryfarmmanager.ui.screens.birds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poultryfarmmanager.data.repository.FirestoreRepository
import com.example.poultryfarmmanager.data.repository.LocalRepository
import com.example.poultryfarmmanager.domain.model.Bird
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BirdsViewModel @Inject constructor(
    private val localRepository: LocalRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _birds = MutableStateFlow<List<Bird>>(emptyList())
    val birds: StateFlow<List<Bird>> = _birds.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    private val _editingBird = MutableStateFlow<Bird?>(null)
    val editingBird = _editingBird.asStateFlow()

    init {
        viewModelScope.launch {
            localRepository.observeBirds().collect { _birds.value = it }
        }
    }

    fun showAddDialog() { _editingBird.value = null; _showAddDialog.value = true }
    fun showEditDialog(bird: Bird) { _editingBird.value = bird; _showAddDialog.value = true }
    fun hideDialog() { _showAddDialog.value = false; _editingBird.value = null }

    fun addBird(breed: String, quantity: Int, sold: Int, price: Double, slaughtered: Int, notes: String) {
        viewModelScope.launch {
            val bird = Bird(breed = breed, quantity = quantity, sold = sold,
                pricePerBird = price, slaughtered = slaughtered, notes = notes)
            localRepository.addBird(bird)
            hideDialog()
            try { firestoreRepository.saveBird(bird) } catch (_: Exception) {}
        }
    }

    fun updateBird(breed: String, quantity: Int, sold: Int, price: Double, slaughtered: Int, notes: String) {
        viewModelScope.launch {
            _editingBird.value?.let { bird ->
                val updated = bird.copy(breed = breed, quantity = quantity, sold = sold,
                    pricePerBird = price, slaughtered = slaughtered, notes = notes)
                localRepository.updateBird(updated)
                hideDialog()
                try { firestoreRepository.saveBird(updated) } catch (_: Exception) {}
            }
        }
    }

    fun deleteBird(bird: Bird) {
        viewModelScope.launch {
            localRepository.deleteBird(bird)
            try { firestoreRepository.deleteBird(bird.id) } catch (_: Exception) {}
        }
    }
}
