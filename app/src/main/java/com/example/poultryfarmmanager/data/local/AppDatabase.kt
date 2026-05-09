package com.example.poultryfarmmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.poultryfarmmanager.domain.model.Bird
import com.example.poultryfarmmanager.domain.model.EggProduction
import com.example.poultryfarmmanager.domain.model.Feed
import com.example.poultryfarmmanager.domain.model.HealthRecord
import com.example.poultryfarmmanager.domain.model.Task

@Database(
    entities = [Bird::class, EggProduction::class, Feed::class, Task::class, HealthRecord::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun birdDao(): BirdDao
    abstract fun eggProductionDao(): EggProductionDao
    abstract fun feedDao(): FeedDao
    abstract fun taskDao(): TaskDao
    abstract fun healthDao(): HealthDao
}
