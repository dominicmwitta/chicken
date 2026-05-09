package com.example.poultryfarmmanager.data.sync

import com.example.poultryfarmmanager.data.repository.FirestoreRepository
import com.example.poultryfarmmanager.data.repository.LocalRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncState { IDLE, SYNCING, SUCCESS, ERROR }

@Singleton
class SyncManager @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val localRepository: LocalRepository,
    private val farmPreferences: FarmPreferences
) {
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _syncMessage = MutableStateFlow("")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    private val listenerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, _ -> }
    )
    private var listenersStarted = false

    init {
        listenerScope.launch {
            try { FirebaseAuth.getInstance().signInAnonymously().await() } catch (_: Exception) {}
            if (farmPreferences.hasFarmId()) startListening()
        }
    }

    fun startListening() {
        if (listenersStarted) return
        listenersStarted = true
        launchListener { firestoreRepository.listenToBirds().collect { localRepository.syncBirdsFromRemote(it) } }
        launchListener { firestoreRepository.listenToEggs().collect { localRepository.syncEggsFromRemote(it) } }
        launchListener { firestoreRepository.listenToFeed().collect { localRepository.syncFeedFromRemote(it) } }
        launchListener { firestoreRepository.listenToTasks().collect { localRepository.syncTasksFromRemote(it) } }
        launchListener { firestoreRepository.listenToHealth().collect { localRepository.syncHealthFromRemote(it) } }
    }

    private fun launchListener(block: suspend () -> Unit) {
        listenerScope.launch {
            while (true) {
                try { block() } catch (_: Exception) {}
                delay(5_000) // wait before restarting a failed listener
            }
        }
    }

    fun resetState() {
        _syncState.value = SyncState.IDLE
        _syncMessage.value = ""
    }

    suspend fun syncAllToFirestore() {
        _syncState.value = SyncState.SYNCING
        _syncMessage.value = "Uploading to cloud..."
        withContext(Dispatchers.IO) {
            try {
                firestoreRepository.syncBirds(localRepository.getAllBirds())
                _syncMessage.value = "Uploading eggs..."
                firestoreRepository.syncEggs(localRepository.getAllEggs())
                _syncMessage.value = "Uploading feed..."
                firestoreRepository.syncFeed(localRepository.getAllFeed())
                _syncMessage.value = "Uploading tasks..."
                firestoreRepository.syncTasks(localRepository.getAllTasks())
                _syncMessage.value = "Uploading health records..."
                firestoreRepository.syncHealth(localRepository.getAllHealth())
                _syncState.value = SyncState.SUCCESS
                _syncMessage.value = "All data uploaded!"
                delay(2000)
                _syncState.value = SyncState.IDLE
                _syncMessage.value = ""
            } catch (e: Exception) {
                _syncState.value = SyncState.ERROR
                _syncMessage.value = "Upload failed: ${e.message}"
            }
        }
    }

    suspend fun loadAllFromFirestore() {
        _syncState.value = SyncState.SYNCING
        _syncMessage.value = "Loading from cloud..."
        withContext(Dispatchers.IO) {
            try {
                localRepository.replaceAllBirds(firestoreRepository.loadBirds())
                _syncMessage.value = "Loading eggs..."
                localRepository.replaceAllEggs(firestoreRepository.loadEggs())
                _syncMessage.value = "Loading feed..."
                localRepository.replaceAllFeed(firestoreRepository.loadFeed())
                _syncMessage.value = "Loading tasks..."
                localRepository.replaceAllTasks(firestoreRepository.loadTasks())
                _syncMessage.value = "Loading health records..."
                localRepository.replaceAllHealth(firestoreRepository.loadHealth())
                _syncState.value = SyncState.SUCCESS
                _syncMessage.value = "Data loaded from cloud!"
                delay(2000)
                _syncState.value = SyncState.IDLE
                _syncMessage.value = ""
            } catch (e: Exception) {
                _syncState.value = SyncState.ERROR
                _syncMessage.value = "Load failed: ${e.message}"
            }
        }
    }
}
