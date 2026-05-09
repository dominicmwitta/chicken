package com.example.poultryfarmmanager.data.local

import androidx.room.*
import com.example.poultryfarmmanager.domain.model.Feed
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM feed ORDER BY name ASC")
    suspend fun getAll(): List<Feed>

    @Query("SELECT * FROM feed ORDER BY name ASC")
    fun observeAll(): Flow<List<Feed>>

    @Query("DELETE FROM feed WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feed: Feed)

    @Update
    suspend fun update(feed: Feed)

    @Delete
    suspend fun delete(feed: Feed)

    @Query("DELETE FROM feed")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(feeds: List<Feed>)
}
