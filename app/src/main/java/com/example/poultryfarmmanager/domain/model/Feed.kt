package com.example.poultryfarmmanager.domain.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "feed")
data class Feed(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val name: String,
    val quantityKg: Double,
    val cost: Double = 0.0,
    val lowStockThreshold: Double = 10.0,
    val lastRestocked: Long = System.currentTimeMillis()
) {
    @get:Ignore val isLowStock: Boolean get() = quantityKg <= lowStockThreshold
}
