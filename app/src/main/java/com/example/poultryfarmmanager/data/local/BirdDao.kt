package com.example.poultryfarmmanager.data.local

import androidx.room.*
import com.example.poultryfarmmanager.domain.model.Bird
import kotlinx.coroutines.flow.Flow

@Dao
interface BirdDao {
    @Query("SELECT * FROM birds ORDER BY dateAcquired DESC")
    suspend fun getAll(): List<Bird>

    @Query("SELECT * FROM birds ORDER BY dateAcquired DESC")
    fun observeAll(): Flow<List<Bird>>

    @Query("DELETE FROM birds WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bird: Bird)

    @Update
    suspend fun update(bird: Bird)

    @Delete
    suspend fun delete(bird: Bird)

    @Query("DELETE FROM birds")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(birds: List<Bird>)
}
