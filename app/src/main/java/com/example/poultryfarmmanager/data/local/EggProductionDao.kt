package com.example.poultryfarmmanager.data.local

import androidx.room.*
import com.example.poultryfarmmanager.domain.model.EggProduction
import kotlinx.coroutines.flow.Flow

@Dao
interface EggProductionDao {
    @Query("SELECT * FROM egg_production ORDER BY date DESC")
    suspend fun getAll(): List<EggProduction>

    @Query("SELECT * FROM egg_production ORDER BY date DESC")
    fun observeAll(): Flow<List<EggProduction>>

    @Query("DELETE FROM egg_production WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(egg: EggProduction)

    @Update
    suspend fun update(egg: EggProduction)

    @Delete
    suspend fun delete(egg: EggProduction)

    @Query("DELETE FROM egg_production")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(eggs: List<EggProduction>)
}
