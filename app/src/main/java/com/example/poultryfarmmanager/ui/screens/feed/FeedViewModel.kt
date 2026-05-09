package com.example.poultryfarmmanager.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poultryfarmmanager.data.repository.FirestoreRepository
import com.example.poultryfarmmanager.data.repository.LocalRepository
import com.example.poultryfarmmanager.domain.model.Feed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val localRepository: LocalRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _feed = MutableStateFlow<List<Feed>>(emptyList())
    val feed: StateFlow<List<Feed>> = _feed.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    private val _editingFeed = MutableStateFlow<Feed?>(null)
    val editingFeed = _editingFeed.asStateFlow()

    private val _showConsumptionDialog = MutableStateFlow(false)
    val showConsumptionDialog = _showConsumptionDialog.asStateFlow()

    private val _selectedFeed = MutableStateFlow<Feed?>(null)
    val selectedFeed: StateFlow<Feed?> = _selectedFeed.asStateFlow()

    init {
        viewModelScope.launch {
            localRepository.observeFeed().collect { _feed.value = it }
        }
    }

    fun showAddDialog() { _editingFeed.value = null; _showAddDialog.value = true }
    fun showEditDialog(f: Feed) { _editingFeed.value = f; _showAddDialog.value = true }
    fun hideAddDialog() { _showAddDialog.value = false; _editingFeed.value = null }

    fun showConsumptionDialog(f: Feed) { _selectedFeed.value = f; _showConsumptionDialog.value = true }
    fun hideConsumptionDialog() { _showConsumptionDialog.value = false; _selectedFeed.value = null }

    fun addFeed(name: String, qty: Double, threshold: Double, cost: Double) {
        viewModelScope.launch {
            val feed = Feed(name = name, quantityKg = qty, lowStockThreshold = threshold, cost = cost)
            localRepository.addFeed(feed)
            hideAddDialog()
            try { firestoreRepository.saveFeed(feed) } catch (_: Exception) {}
        }
    }

    fun updateFeed(f: Feed, name: String, qty: Double, threshold: Double, cost: Double) {
        viewModelScope.launch {
            val updated = f.copy(name = name, quantityKg = qty, lowStockThreshold = threshold, cost = cost)
            localRepository.updateFeed(updated)
            hideAddDialog()
            try { firestoreRepository.saveFeed(updated) } catch (_: Exception) {}
        }
    }

    fun deleteFeed(f: Feed) {
        viewModelScope.launch {
            localRepository.deleteFeed(f)
            try { firestoreRepository.deleteFeed(f.id) } catch (_: Exception) {}
        }
    }

    fun consumeFeed(f: Feed, qty: Double) {
        viewModelScope.launch {
            val updated = f.copy(quantityKg = (f.quantityKg - qty).coerceAtLeast(0.0))
            localRepository.updateFeed(updated)
            hideConsumptionDialog()
            try { firestoreRepository.saveFeed(updated) } catch (_: Exception) {}
        }
    }
}
