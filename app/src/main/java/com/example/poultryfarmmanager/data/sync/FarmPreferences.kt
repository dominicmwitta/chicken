package com.example.poultryfarmmanager.data.sync

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs = context.getSharedPreferences("farm_prefs", Context.MODE_PRIVATE)

    fun getFarmId(): String? = prefs.getString("farm_id", null)
    fun setFarmId(id: String) = prefs.edit().putString("farm_id", id.trim()).apply()
    fun hasFarmId(): Boolean = !getFarmId().isNullOrBlank()

    fun getLanguage(): String = prefs.getString("language", "sw") ?: "sw"
    fun setLanguage(lang: String) = prefs.edit().putString("language", lang).apply()
}
