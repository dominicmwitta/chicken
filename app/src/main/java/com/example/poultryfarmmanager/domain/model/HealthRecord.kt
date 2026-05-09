package com.example.poultryfarmmanager.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val type: String,           // "Vaccine", "Medication", "Supplement", "Dewormer", "Other"
    val name: String,
    val date: Long = System.currentTimeMillis(),
    val cost: Double = 0.0,
    val dosage: String = "",
    val birdsAffected: Int = 0,
    val notes: String = ""
)
