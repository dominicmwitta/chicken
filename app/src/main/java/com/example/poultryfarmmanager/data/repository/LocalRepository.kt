package com.example.poultryfarmmanager.data.repository

import com.example.poultryfarmmanager.data.local.*
import com.example.poultryfarmmanager.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
    private val birdDao: BirdDao,
    private val eggDao: EggProductionDao,
    private val feedDao: FeedDao,
    private val taskDao: TaskDao,
    private val healthDao: HealthDao
) {
    // --- Birds ---
    suspend fun getAllBirds(): List<Bird> = withContext(Dispatchers.IO) { birdDao.getAll() }
    fun observeBirds(): Flow<List<Bird>> = birdDao.observeAll()
    suspend fun addBird(bird: Bird) = withContext(Dispatchers.IO) { birdDao.insert(bird) }
    suspend fun updateBird(bird: Bird) = withContext(Dispatchers.IO) { birdDao.update(bird) }
    suspend fun deleteBird(bird: Bird) = withContext(Dispatchers.IO) { birdDao.delete(bird) }
    suspend fun replaceAllBirds(birds: List<Bird>) = withContext(Dispatchers.IO) { birdDao.deleteAll(); birdDao.insertAll(birds) }
    suspend fun syncBirdsFromRemote(birds: List<Bird>) = withContext(Dispatchers.IO) {
        if (birds.isEmpty()) return@withContext
        val existingIds = birdDao.getAll().map { it.id }.toSet()
        val newIds = birds.map { it.id }.toSet()
        (existingIds - newIds).forEach { birdDao.deleteById(it) }
        birdDao.insertAll(birds)
    }

    // --- Eggs ---
    suspend fun getAllEggs(): List<EggProduction> = withContext(Dispatchers.IO) { eggDao.getAll() }
    fun observeEggs(): Flow<List<EggProduction>> = eggDao.observeAll()
    suspend fun addEgg(egg: EggProduction) = withContext(Dispatchers.IO) { eggDao.insert(egg) }
    suspend fun updateEgg(egg: EggProduction) = withContext(Dispatchers.IO) { eggDao.update(egg) }
    suspend fun deleteEgg(egg: EggProduction) = withContext(Dispatchers.IO) { eggDao.delete(egg) }
    suspend fun replaceAllEggs(eggs: List<EggProduction>) = withContext(Dispatchers.IO) { eggDao.deleteAll(); eggDao.insertAll(eggs) }
    suspend fun syncEggsFromRemote(eggs: List<EggProduction>) = withContext(Dispatchers.IO) {
        if (eggs.isEmpty()) return@withContext
        val existingIds = eggDao.getAll().map { it.id }.toSet()
        val newIds = eggs.map { it.id }.toSet()
        (existingIds - newIds).forEach { eggDao.deleteById(it) }
        eggDao.insertAll(eggs)
    }

    // --- Feed ---
    suspend fun getAllFeed(): List<Feed> = withContext(Dispatchers.IO) { feedDao.getAll() }
    fun observeFeed(): Flow<List<Feed>> = feedDao.observeAll()
    suspend fun addFeed(feed: Feed) = withContext(Dispatchers.IO) { feedDao.insert(feed) }
    suspend fun updateFeed(feed: Feed) = withContext(Dispatchers.IO) { feedDao.update(feed) }
    suspend fun deleteFeed(feed: Feed) = withContext(Dispatchers.IO) { feedDao.delete(feed) }
    suspend fun replaceAllFeed(feeds: List<Feed>) = withContext(Dispatchers.IO) { feedDao.deleteAll(); feedDao.insertAll(feeds) }
    suspend fun syncFeedFromRemote(feeds: List<Feed>) = withContext(Dispatchers.IO) {
        if (feeds.isEmpty()) return@withContext
        val existingIds = feedDao.getAll().map { it.id }.toSet()
        val newIds = feeds.map { it.id }.toSet()
        (existingIds - newIds).forEach { feedDao.deleteById(it) }
        feedDao.insertAll(feeds)
    }

    // --- Tasks ---
    suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) { taskDao.getAll() }
    fun observeTasks(): Flow<List<Task>> = taskDao.observeAll()
    suspend fun addTask(task: Task) = withContext(Dispatchers.IO) { taskDao.insert(task) }
    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) { taskDao.update(task) }
    suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO) { taskDao.delete(task) }
    suspend fun replaceAllTasks(tasks: List<Task>) = withContext(Dispatchers.IO) { taskDao.deleteAll(); taskDao.insertAll(tasks) }
    suspend fun syncTasksFromRemote(tasks: List<Task>) = withContext(Dispatchers.IO) {
        if (tasks.isEmpty()) return@withContext
        val existingIds = taskDao.getAll().map { it.id }.toSet()
        val newIds = tasks.map { it.id }.toSet()
        (existingIds - newIds).forEach { taskDao.deleteById(it) }
        taskDao.insertAll(tasks)
    }

    // --- Health ---
    suspend fun getAllHealth(): List<HealthRecord> = withContext(Dispatchers.IO) { healthDao.getAll() }
    fun observeHealth(): Flow<List<HealthRecord>> = healthDao.observeAll()
    suspend fun addHealth(record: HealthRecord) = withContext(Dispatchers.IO) { healthDao.insert(record) }
    suspend fun updateHealth(record: HealthRecord) = withContext(Dispatchers.IO) { healthDao.update(record) }
    suspend fun deleteHealth(record: HealthRecord) = withContext(Dispatchers.IO) { healthDao.delete(record) }
    suspend fun replaceAllHealth(records: List<HealthRecord>) = withContext(Dispatchers.IO) { healthDao.deleteAll(); healthDao.insertAll(records) }
    suspend fun syncHealthFromRemote(records: List<HealthRecord>) = withContext(Dispatchers.IO) {
        if (records.isEmpty()) return@withContext
        val existingIds = healthDao.getAll().map { it.id }.toSet()
        val newIds = records.map { it.id }.toSet()
        (existingIds - newIds).forEach { healthDao.deleteById(it) }
        healthDao.insertAll(records)
    }
}
