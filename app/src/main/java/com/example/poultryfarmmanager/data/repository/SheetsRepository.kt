package com.example.poultryfarmmanager.data.repository

import com.example.poultryfarmmanager.data.sync.GoogleSheetsSync
import com.example.poultryfarmmanager.data.sync.SheetsConfig
import com.example.poultryfarmmanager.domain.model.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SheetsRepository @Inject constructor(
    private val sheetsSync: GoogleSheetsSync
) {
    var token: String = ""

    // --- Birds ---

    suspend fun getAllBirds(): List<Bird> {
        val rows = sheetsSync.getValues(token, SheetsConfig.SHEET_BIRDS)
        if (rows.size <= 1) return emptyList()
        return rows.drop(1).mapNotNull { parseBird(it) }
    }

    suspend fun addBird(bird: Bird) {
        val all = getAllBirds().toMutableList()
        all.add(0, bird)
        writeBirds(all)
    }

    suspend fun updateBird(bird: Bird) {
        val all = getAllBirds().toMutableList()
        val idx = all.indexOfFirst { it.id == bird.id }
        if (idx >= 0) all[idx] = bird
        writeBirds(all)
    }

    suspend fun deleteBird(bird: Bird) {
        val all = getAllBirds().filter { it.id != bird.id }
        writeBirds(all)
    }

    private suspend fun writeBirds(birds: List<Bird>) {
        val data = birds.map { bird ->
            listOf<Any>(bird.id, bird.breed, bird.quantity, bird.sold,
                bird.pricePerBird, bird.slaughtered, formatTimestamp(bird.dateAcquired), bird.notes)
        }
        sheetsSync.writeBirds(token, data)
    }

    private fun parseBird(row: List<Any>): Bird? {
        return try {
            Bird(
                id = row.getOrNull(0)?.toString()?.toLongOrNull() ?: return null,
                breed = row.getOrNull(1)?.toString() ?: "",
                quantity = row.getOrNull(2)?.toString()?.toIntOrNull() ?: 0,
                sold = row.getOrNull(3)?.toString()?.toIntOrNull() ?: 0,
                pricePerBird = row.getOrNull(4)?.toString()?.toDoubleOrNull() ?: 0.0,
                slaughtered = row.getOrNull(5)?.toString()?.toIntOrNull() ?: 0,
                dateAcquired = parseDate(row.getOrNull(6)?.toString()),
                notes = row.getOrNull(7)?.toString() ?: ""
            )
        } catch (_: Exception) { null }
    }

    // --- Egg Production ---

    suspend fun getAllEggs(): List<EggProduction> {
        val rows = sheetsSync.getValues(token, SheetsConfig.SHEET_EGGS)
        if (rows.size <= 1) return emptyList()
        return rows.drop(1).mapNotNull { parseEgg(it) }
    }

    suspend fun addEgg(egg: EggProduction) {
        val all = getAllEggs().toMutableList()
        all.add(0, egg)
        writeEggs(all)
    }

    suspend fun updateEgg(egg: EggProduction) {
        val all = getAllEggs().toMutableList()
        val idx = all.indexOfFirst { it.id == egg.id }
        if (idx >= 0) all[idx] = egg
        writeEggs(all)
    }

    suspend fun deleteEgg(egg: EggProduction) {
        val all = getAllEggs().filter { it.id != egg.id }
        writeEggs(all)
    }

    private suspend fun writeEggs(eggs: List<EggProduction>) {
        val data = eggs.map { egg ->
            listOf<Any>(egg.id, formatTimestamp(egg.date), egg.totalEggs,
                egg.eggsSold, egg.pricePerEgg, egg.eggsConsumed, egg.notes)
        }
        sheetsSync.writeEggs(token, data)
    }

    private fun parseEgg(row: List<Any>): EggProduction? {
        return try {
            EggProduction(
                id = row.getOrNull(0)?.toString()?.toLongOrNull() ?: return null,
                date = parseDate(row.getOrNull(1)?.toString()),
                totalEggs = row.getOrNull(2)?.toString()?.toIntOrNull() ?: 0,
                eggsSold = row.getOrNull(3)?.toString()?.toIntOrNull() ?: 0,
                pricePerEgg = row.getOrNull(4)?.toString()?.toDoubleOrNull() ?: 0.0,
                eggsConsumed = row.getOrNull(5)?.toString()?.toIntOrNull() ?: 0,
                notes = row.getOrNull(6)?.toString() ?: ""
            )
        } catch (_: Exception) { null }
    }

    // --- Feed ---

    suspend fun getAllFeed(): List<Feed> {
        val rows = sheetsSync.getValues(token, SheetsConfig.SHEET_FEED)
        if (rows.size <= 1) return emptyList()
        return rows.drop(1).mapNotNull { parseFeed(it) }
    }

    suspend fun addFeed(feed: Feed) {
        val all = getAllFeed().toMutableList()
        all.add(0, feed)
        writeFeed(all)
    }

    suspend fun updateFeed(feed: Feed) {
        val all = getAllFeed().toMutableList()
        val idx = all.indexOfFirst { it.id == feed.id }
        if (idx >= 0) all[idx] = feed
        writeFeed(all)
    }

    suspend fun deleteFeed(feed: Feed) {
        val all = getAllFeed().filter { it.id != feed.id }
        writeFeed(all)
    }

    private suspend fun writeFeed(feeds: List<Feed>) {
        val data = feeds.map { feed ->
            listOf<Any>(feed.id, feed.name, feed.quantityKg, feed.cost,
                feed.lowStockThreshold, formatTimestamp(feed.lastRestocked))
        }
        sheetsSync.writeFeed(token, data)
    }

    private fun parseFeed(row: List<Any>): Feed? {
        return try {
            Feed(
                id = row.getOrNull(0)?.toString()?.toLongOrNull() ?: return null,
                name = row.getOrNull(1)?.toString() ?: "",
                quantityKg = row.getOrNull(2)?.toString()?.toDoubleOrNull() ?: 0.0,
                cost = row.getOrNull(3)?.toString()?.toDoubleOrNull() ?: 0.0,
                lowStockThreshold = row.getOrNull(4)?.toString()?.toDoubleOrNull() ?: 10.0,
                lastRestocked = parseDate(row.getOrNull(5)?.toString())
            )
        } catch (_: Exception) { null }
    }

    // --- Tasks ---

    suspend fun getAllTasks(): List<Task> {
        val rows = sheetsSync.getValues(token, SheetsConfig.SHEET_TASKS)
        if (rows.size <= 1) return emptyList()
        return rows.drop(1).mapNotNull { parseTask(it) }
    }

    suspend fun addTask(task: Task) {
        val all = getAllTasks().toMutableList()
        all.add(0, task)
        writeTasks(all)
    }

    suspend fun updateTask(task: Task) {
        val all = getAllTasks().toMutableList()
        val idx = all.indexOfFirst { it.id == task.id }
        if (idx >= 0) all[idx] = task
        writeTasks(all)
    }

    suspend fun deleteTask(task: Task) {
        val all = getAllTasks().filter { it.id != task.id }
        writeTasks(all)
    }

    private suspend fun writeTasks(tasks: List<Task>) {
        val data = tasks.map { task ->
            listOf<Any>(task.id, task.title, task.description,
                formatTimestamp(task.dueDate), if (task.isCompleted) "Yes" else "No",
                formatTimestamp(task.createdAt))
        }
        sheetsSync.writeTasks(token, data)
    }

    private fun parseTask(row: List<Any>): Task? {
        return try {
            Task(
                id = row.getOrNull(0)?.toString()?.toLongOrNull() ?: return null,
                title = row.getOrNull(1)?.toString() ?: "",
                description = row.getOrNull(2)?.toString() ?: "",
                dueDate = parseDate(row.getOrNull(3)?.toString()),
                isCompleted = row.getOrNull(4)?.toString()?.equals("Yes", true) == true,
                createdAt = parseDate(row.getOrNull(5)?.toString())
            )
        } catch (_: Exception) { null }
    }

    // --- Sync from local to Sheets ---

    suspend fun syncBirds(birds: List<Bird>) {
        if (token.isEmpty()) return
        val data = birds.map { b ->
            listOf<Any>(b.id, b.breed, b.quantity, b.sold,
                b.pricePerBird, b.slaughtered, formatTimestamp(b.dateAcquired), b.notes)
        }
        sheetsSync.writeBirds(token, data)
    }

    suspend fun syncEggs(eggs: List<EggProduction>) {
        if (token.isEmpty()) return
        val data = eggs.map { e ->
            listOf<Any>(e.id, formatTimestamp(e.date), e.totalEggs,
                e.eggsSold, e.pricePerEgg, e.eggsConsumed, e.notes)
        }
        sheetsSync.writeEggs(token, data)
    }

    suspend fun syncFeed(feeds: List<Feed>) {
        if (token.isEmpty()) return
        val data = feeds.map { f ->
            listOf<Any>(f.id, f.name, f.quantityKg, f.cost,
                f.lowStockThreshold, formatTimestamp(f.lastRestocked))
        }
        sheetsSync.writeFeed(token, data)
    }

    suspend fun syncTasks(tasks: List<Task>) {
        if (token.isEmpty()) return
        val data = tasks.map { t ->
            listOf<Any>(t.id, t.title, t.description,
                formatTimestamp(t.dueDate), if (t.isCompleted) "Yes" else "No",
                formatTimestamp(t.createdAt))
        }
        sheetsSync.writeTasks(token, data)
    }

    // --- Utils ---

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun parseDate(str: String?): Long {
        if (str == null) return System.currentTimeMillis()
        return try {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            sdf.parse(str)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            str.toLongOrNull() ?: System.currentTimeMillis()
        }
    }
}