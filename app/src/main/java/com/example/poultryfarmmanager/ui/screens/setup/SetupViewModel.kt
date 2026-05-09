package com.example.poultryfarmmanager.ui.screens.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poultryfarmmanager.data.sync.FarmPreferences
import com.example.poultryfarmmanager.data.sync.SyncManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val farmPreferences: FarmPreferences,
    private val syncManager: SyncManager
) : ViewModel() {

    fun hasFarmId(): Boolean = farmPreferences.hasFarmId()

    fun joinFarm(farmId: String) {
        farmPreferences.setFarmId(farmId)
        viewModelScope.launch {
            try { FirebaseAuth.getInstance().signInAnonymously().await() } catch (_: Exception) {}
            syncManager.startListening()
        }
    }
}
