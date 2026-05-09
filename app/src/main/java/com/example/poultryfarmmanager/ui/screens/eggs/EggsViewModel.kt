package com.example.poultryfarmmanager.ui.screens.eggs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poultryfarmmanager.data.repository.FirestoreRepository
import com.example.poultryfarmmanager.data.repository.LocalRepository
import com.example.poultryfarmmanager.domain.model.EggProduction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class EggsViewModel @Inject constructor(
    private val localRepository: LocalRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _eggs = MutableStateFlow<List<EggProduction>>(emptyList())
    val eggs: StateFlow<List<EggProduction>> = _eggs.asStateFlow()

    private val _todayProd = MutableStateFlow<EggProduction?>(null)
    val todayProduction: StateFlow<EggProduction?> = _todayProd.asStateFlow()

    private val _todayTotal = MutableStateFlow(0)
    val todayTotalEggs: StateFlow<Int> = _todayTotal.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    init {
        viewModelScope.launch {
            localRepository.observeEggs().collect { all ->
                _eggs.value = all
                val todayStart = getTodayStart()
                _todayProd.value = all.find { it.date >= todayStart }
                _todayTotal.value = all.filter { it.date >= todayStart }.sumOf { it.totalEggs }
            }
        }
    }

    fun showAddDialog() { _showAddDialog.value = true }
    fun hideDialog() { _showAddDialog.value = false }

    fun addEgg(totalEggs: Int, eggsSold: Int, price: Double, consumed: Int, notes: String) {
        viewModelScope.launch {
            val egg = EggProduction(totalEggs = totalEggs, eggsSold = eggsSold,
                pricePerEgg = price, eggsConsumed = consumed, notes = notes)
            localRepository.addEgg(egg)
            hideDialog()
            try { firestoreRepository.saveEgg(egg) } catch (_: Exception) {}
        }
    }

    fun deleteEgg(egg: EggProduction) {
        viewModelScope.launch {
            localRepository.deleteEgg(egg)
            try { firestoreRepository.deleteEgg(egg.id) } catch (_: Exception) {}
        }
    }

    private fun getTodayStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
