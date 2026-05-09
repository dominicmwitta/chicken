package com.example.poultryfarmmanager.data.local

import androidx.room.*
import com.example.poultryfarmmanager.domain.model.HealthRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_records ORDER BY date DESC")
    suspend fun getAll(): List<HealthRecord>

    @Query("SELECT * FROM health_records ORDER BY date DESC")
    fun observeAll(): Flow<List<HealthRecord>>

    @Query("DELETE FROM health_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HealthRecord)

    @Update
    suspend fun update(record: HealthRecord)

    @Delete
    suspend fun delete(record: HealthRecord)

    @Query("DELETE FROM health_records")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HealthRecord>)
}
