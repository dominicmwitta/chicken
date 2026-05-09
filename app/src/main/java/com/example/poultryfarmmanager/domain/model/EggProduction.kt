package com.example.poultryfarmmanager.domain.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "egg_production")
data class EggProduction(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val date: Long = System.currentTimeMillis(),
    val totalEggs: Int = 0,
    val eggsSold: Int = 0,
    val pricePerEgg: Double = 0.0,
    val eggsConsumed: Int = 0,
    val notes: String = ""
) {
    @get:Ignore val remaining: Int get() = totalEggs - eggsSold - eggsConsumed
    @get:Ignore val revenue: Double get() = eggsSold * pricePerEgg
}
