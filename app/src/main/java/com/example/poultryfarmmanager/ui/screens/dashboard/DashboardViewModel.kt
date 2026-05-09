package com.example.poultryfarmmanager.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poultryfarmmanager.data.repository.LocalRepository
import com.example.poultryfarmmanager.data.sync.SyncManager
import com.example.poultryfarmmanager.data.sync.SyncState
import com.example.poultryfarmmanager.domain.model.Bird
import com.example.poultryfarmmanager.domain.model.EggProduction
import com.example.poultryfarmmanager.domain.model.Feed
import com.example.poultryfarmmanager.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val localRepository: LocalRepository,
    val syncManager: SyncManager
) : ViewModel() {

    private val _totalBirds = MutableStateFlow(0)
    val totalBirds: StateFlow<Int> = _totalBirds.asStateFlow()

    private val _todayEggs = MutableStateFlow(0)
    val todayEggs: StateFlow<Int> = _todayEggs.asStateFlow()

    private val _pendingTasks = MutableStateFlow(0)
    val pendingTasks: StateFlow<Int> = _pendingTasks.asStateFlow()

    private val _lowStockCount = MutableStateFlow(0)
    val lowStockFeedCount: StateFlow<Int> = _lowStockCount.asStateFlow()

    private val _selectedDays = MutableStateFlow(30)
    val selectedDays: StateFlow<Int> = _selectedDays.asStateFlow()

    private val _analytics = MutableStateFlow(FarmAnalytics())
    val analytics: StateFlow<FarmAnalytics> = _analytics.asStateFlow()

    val syncState: StateFlow<SyncState> = syncManager.syncState
    val syncMessage: StateFlow<String> = syncManager.syncMessage

    init {
        viewModelScope.launch {
            combine(
                localRepository.observeBirds(),
                localRepository.observeEggs(),
                localRepository.observeFeed(),
                localRepository.observeTasks()
            ) { birds, eggs, feeds, tasks ->
                FarmSnapshot(birds.toList(), eggs.toList(), feeds.toList(), tasks.toList())
            }.collect { snapshot ->
                updateAll(snapshot)
            }
        }
    }

    fun setSelectedDays(days: Int) {
        _selectedDays.value = days
        // Re-trigger by re-collecting would be complex; easier to just recompute from last snapshot
        viewModelScope.launch {
            val birds = localRepository.getAllBirds()
            val eggs = localRepository.getAllEggs()
            val feeds = localRepository.getAllFeed()
            val tasks = localRepository.getAllTasks()
            updateAll(FarmSnapshot(birds, eggs, feeds, tasks))
        }
    }

    private fun updateAll(s: FarmSnapshot) {
        val days = _selectedDays.value
        val todayStart = dayStart(System.currentTimeMillis())
        val rangeStart = todayStart - days.toLong() * 24 * 60 * 60 * 1000

        _totalBirds.value = s.birds.sumOf { it.remaining }
        _todayEggs.value = s.eggs.filter { it.date >= todayStart }.sumOf { it.totalEggs }
        _pendingTasks.value = s.tasks.count { !it.isCompleted }
        _lowStockCount.value = s.feeds.count { it.isLowStock }

        val eggsInRange = s.eggs.filter { it.date >= rangeStart }
        _analytics.value = FarmAnalytics(
            selectedDays = days,
            eggRevenue = eggsInRange.sumOf { it.eggsSold * it.pricePerEgg },
            birdRevenue = s.birds.sumOf { it.sold * it.pricePerBird },
            totalEggs = eggsInRange.sumOf { it.totalEggs },
            eggTrend = buildEggTrend(s.eggs, days, todayStart),
            birdBreakdown = s.birds.groupBy { it.breed }
                .map { (breed, list) -> BreedCount(breed, list.sumOf { it.remaining }) }
                .sortedByDescending { it.remaining },
            totalBirdsSold = s.birds.sumOf { it.sold },
            totalBirdsSlaughtered = s.birds.sumOf { it.slaughtered },
            totalFeedCost = s.feeds.sumOf { it.cost },
            feedTypeCount = s.feeds.size,
            lowStockCount = s.feeds.count { it.isLowStock }
        )
    }

    private fun buildEggTrend(eggs: List<EggProduction>, days: Int, todayStart: Long): List<EggDataPoint> {
        return if (days <= 30) {
            val fmt = if (days <= 7) SimpleDateFormat("EEE", Locale.getDefault())
                      else SimpleDateFormat("d", Locale.getDefault())
            (days - 1 downTo 0).map { daysBack ->
                val dayMs = todayStart - daysBack.toLong() * 24 * 60 * 60 * 1000
                val nextDay = dayMs + 24L * 60 * 60 * 1000
                EggDataPoint(fmt.format(Date(dayMs)),
                    eggs.filter { it.date >= dayMs && it.date < nextDay }.sumOf { it.totalEggs })
            }
        } else {
            val weeks = days / 7
            (weeks - 1 downTo 0).mapIndexed { _, weeksBack ->
                val weekEnd = todayStart - weeksBack.toLong() * 7 * 24 * 60 * 60 * 1000
                val weekStart = weekEnd - 7L * 24 * 60 * 60 * 1000
                EggDataPoint("W${weeks - weeksBack}",
                    eggs.filter { it.date >= weekStart && it.date < weekEnd }.sumOf { it.totalEggs })
            }
        }
    }

    fun uploadToCloud() {
        viewModelScope.launch { syncManager.syncAllToFirestore() }
    }

    fun downloadFromCloud() {
        viewModelScope.launch { syncManager.loadAllFromFirestore() }
    }

    fun resetSyncState() { syncManager.resetState() }

    private fun dayStart(timestamp: Long): Long = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private data class FarmSnapshot(
        val birds: List<Bird>,
        val eggs: List<EggProduction>,
        val feeds: List<Feed>,
        val tasks: List<Task>
    )
}
