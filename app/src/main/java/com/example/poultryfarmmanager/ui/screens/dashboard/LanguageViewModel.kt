package com.example.poultryfarmmanager.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import com.example.poultryfarmmanager.data.sync.FarmPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val farmPreferences: FarmPreferences
) : ViewModel() {

    private val _language = MutableStateFlow(farmPreferences.getLanguage())
    val language = _language.asStateFlow()

    fun toggle() {
        val next = if (_language.value == "sw") "en" else "sw"
        farmPreferences.setLanguage(next)
        _language.value = next
    }
}
