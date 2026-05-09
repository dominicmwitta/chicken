package com.example.poultryfarmmanager.domain.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "birds")
data class Bird(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val breed: String,
    val quantity: Int,
    val dateAcquired: Long = System.currentTimeMillis(),
    val sold: Int = 0,
    val pricePerBird: Double = 0.0,
    val slaughtered: Int = 0,
    val notes: String = ""
) {
    @get:Ignore val remaining: Int get() = quantity - sold - slaughtered
    @get:Ignore val revenue: Double get() = sold * pricePerBird
}
