package com.example.poultryfarmmanager.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val title: String,
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
