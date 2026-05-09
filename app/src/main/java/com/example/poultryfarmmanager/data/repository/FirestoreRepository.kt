package com.example.poultryfarmmanager.data.repository

import com.example.poultryfarmmanager.data.sync.FarmPreferences
import com.example.poultryfarmmanager.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val farmPreferences: FarmPreferences
) {
    private val db = FirebaseFirestore.getInstance()

    private fun farmDoc() = db.collection("farms").document(farmPreferences.getFarmId() ?: "default")
    private fun birdsCol() = farmDoc().collection("birds")
    private fun eggsCol() = farmDoc().collection("eggs")
    private fun feedCol() = farmDoc().collection("feed")
    private fun tasksCol() = farmDoc().collection("tasks")
    private fun healthCol() = farmDoc().collection("health")

    // --- Real-time listeners ---

    fun listenToBirds(): Flow<List<Bird>> = callbackFlow {
        val reg = birdsCol().addSnapshotListener { snap, err ->
            if (err != null || snap == null || snap.metadata.isFromCache) return@addSnapshotListener
            trySend(snap.documents.mapNotNull { mapToBird(it.data) })
        }
        awaitClose { reg.remove() }
    }

    fun listenToEggs(): Flow<List<EggProduction>> = callbackFlow {
        val reg = eggsCol().addSnapshotListener { snap, err ->
            if (err != null || snap == null || snap.metadata.isFromCache) return@addSnapshotListener
            trySend(snap.documents.mapNotNull { mapToEgg(it.data) })
        }
        awaitClose { reg.remove() }
    }

    fun listenToFeed(): Flow<List<Feed>> = callbackFlow {
        val reg = feedCol().addSnapshotListener { snap, err ->
            if (err != null || snap == null || snap.metadata.isFromCache) return@addSnapshotListener
            trySend(snap.documents.mapNotNull { mapToFeed(it.data) })
        }
        awaitClose { reg.remove() }
    }

    fun listenToTasks(): Flow<List<Task>> = callbackFlow {
        val reg = tasksCol().addSnapshotListener { snap, err ->
            if (err != null || snap == null || snap.metadata.isFromCache) return@addSnapshotListener
            trySend(snap.documents.mapNotNull { mapToTask(it.data) })
        }
        awaitClose { reg.remove() }
    }

    // --- Single-document writes (used by ViewModels on each mutation) ---

    suspend fun saveBird(bird: Bird) = withContext(Dispatchers.IO) {
        ensureAuth()
        birdsCol().document(bird.id.toString()).set(birdToMap(bird)).await()
    }

    suspend fun deleteBird(id: Long) = withContext(Dispatchers.IO) {
        ensureAuth()
        birdsCol().document(id.toString()).delete().await()
    }

    suspend fun saveEgg(egg: EggProduction) = withContext(Dispatchers.IO) {
        ensureAuth()
        eggsCol().document(egg.id.toString()).set(eggToMap(egg)).await()
    }

    suspend fun deleteEgg(id: Long) = withContext(Dispatchers.IO) {
        ensureAuth()
        eggsCol().document(id.toString()).delete().await()
    }

    suspend fun saveFeed(feed: Feed) = withContext(Dispatchers.IO) {
        ensureAuth()
        feedCol().document(feed.id.toString()).set(feedToMap(feed)).await()
    }

    suspend fun deleteFeed(id: Long) = withContext(Dispatchers.IO) {
        ensureAuth()
        feedCol().document(id.toString()).delete().await()
    }

    suspend fun saveTask(task: Task) = withContext(Dispatchers.IO) {
        ensureAuth()
        tasksCol().document(task.id.toString()).set(taskToMap(task)).await()
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        ensureAuth()
        tasksCol().document(id.toString()).delete().await()
    }

    suspend fun saveHealth(record: HealthRecord) = withContext(Dispatchers.IO) {
        ensureAuth()
        healthCol().document(record.id.toString()).set(healthToMap(record)).await()
    }

    suspend fun deleteHealth(id: Long) = withContext(Dispatchers.IO) {
        ensureAuth()
        healthCol().document(id.toString()).delete().await()
    }

    // --- Bulk sync (used by manual Upload button and SyncManager) ---

    suspend fun syncBirds(birds: List<Bird>) = batchSync(
        birdsCol(), birds.map { it.id.toString() }.toSet()
    ) { batch, col -> birds.forEach { batch.set(col.document(it.id.toString()), birdToMap(it)) } }

    suspend fun syncEggs(eggs: List<EggProduction>) = batchSync(
        eggsCol(), eggs.map { it.id.toString() }.toSet()
    ) { batch, col -> eggs.forEach { batch.set(col.document(it.id.toString()), eggToMap(it)) } }

    suspend fun syncFeed(feeds: List<Feed>) = batchSync(
        feedCol(), feeds.map { it.id.toString() }.toSet()
    ) { batch, col -> feeds.forEach { batch.set(col.document(it.id.toString()), feedToMap(it)) } }

    suspend fun syncTasks(tasks: List<Task>) = batchSync(
        tasksCol(), tasks.map { it.id.toString() }.toSet()
    ) { batch, col -> tasks.forEach { batch.set(col.document(it.id.toString()), taskToMap(it)) } }

    suspend fun syncHealth(records: List<HealthRecord>) = batchSync(
        healthCol(), records.map { it.id.toString() }.toSet()
    ) { batch, col -> records.forEach { batch.set(col.document(it.id.toString()), healthToMap(it)) } }

    suspend fun loadHealth(): List<HealthRecord> = withContext(Dispatchers.IO) {
        healthCol().get().await().documents.mapNotNull { mapToHealth(it.data) }
    }

    fun listenToHealth(): Flow<List<HealthRecord>> = callbackFlow {
        val reg = healthCol().addSnapshotListener { snap, err ->
            if (err != null || snap == null || snap.metadata.isFromCache) return@addSnapshotListener
            trySend(snap.documents.mapNotNull { mapToHealth(it.data) })
        }
        awaitClose { reg.remove() }
    }

    // --- Load (pull Firestore → caller) ---

    suspend fun loadBirds(): List<Bird> = withContext(Dispatchers.IO) {
        birdsCol().get().await().documents.mapNotNull { mapToBird(it.data) }
    }

    suspend fun loadEggs(): List<EggProduction> = withContext(Dispatchers.IO) {
        eggsCol().get().await().documents.mapNotNull { mapToEgg(it.data) }
    }

    suspend fun loadFeed(): List<Feed> = withContext(Dispatchers.IO) {
        feedCol().get().await().documents.mapNotNull { mapToFeed(it.data) }
    }

    suspend fun loadTasks(): List<Task> = withContext(Dispatchers.IO) {
        tasksCol().get().await().documents.mapNotNull { mapToTask(it.data) }
    }

    private suspend fun ensureAuth() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously().await()
        }
    }

    private suspend fun batchSync(
        col: CollectionReference,
        newIds: Set<String>,
        addWrites: (com.google.firebase.firestore.WriteBatch, CollectionReference) -> Unit
    ) = withContext(Dispatchers.IO) {
        ensureAuth()
        val existing = col.get().await().documents
        val batch = db.batch()
        existing.filter { it.id !in newIds }.forEach { batch.delete(it.reference) }
        addWrites(batch, col)
        batch.commit().await()
    }

    // --- Mapping ---

    private fun birdToMap(b: Bird): Map<String, Any> = mapOf(
        "id" to b.id, "breed" to b.breed, "quantity" to b.quantity,
        "sold" to b.sold, "pricePerBird" to b.pricePerBird,
        "slaughtered" to b.slaughtered, "dateAcquired" to b.dateAcquired, "notes" to b.notes
    )

    private fun mapToBird(data: Map<String, Any>?): Bird? {
        data ?: return null
        return try {
            Bird(
                id = (data["id"] as? Number)?.toLong() ?: return null,
                breed = data["breed"] as? String ?: "",
                quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                sold = (data["sold"] as? Number)?.toInt() ?: 0,
                pricePerBird = (data["pricePerBird"] as? Number)?.toDouble() ?: 0.0,
                slaughtered = (data["slaughtered"] as? Number)?.toInt() ?: 0,
                dateAcquired = (data["dateAcquired"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                notes = data["notes"] as? String ?: ""
            )
        } catch (_: Exception) { null }
    }

    private fun eggToMap(e: EggProduction): Map<String, Any> = mapOf(
        "id" to e.id, "date" to e.date, "totalEggs" to e.totalEggs,
        "eggsSold" to e.eggsSold, "pricePerEgg" to e.pricePerEgg,
        "eggsConsumed" to e.eggsConsumed, "notes" to e.notes
    )

    private fun mapToEgg(data: Map<String, Any>?): EggProduction? {
        data ?: return null
        return try {
            EggProduction(
                id = (data["id"] as? Number)?.toLong() ?: return null,
                date = (data["date"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                totalEggs = (data["totalEggs"] as? Number)?.toInt() ?: 0,
                eggsSold = (data["eggsSold"] as? Number)?.toInt() ?: 0,
                pricePerEgg = (data["pricePerEgg"] as? Number)?.toDouble() ?: 0.0,
                eggsConsumed = (data["eggsConsumed"] as? Number)?.toInt() ?: 0,
                notes = data["notes"] as? String ?: ""
            )
        } catch (_: Exception) { null }
    }

    private fun feedToMap(f: Feed): Map<String, Any> = mapOf(
        "id" to f.id, "name" to f.name, "quantityKg" to f.quantityKg,
        "cost" to f.cost, "lowStockThreshold" to f.lowStockThreshold,
        "lastRestocked" to f.lastRestocked
    )

    private fun mapToFeed(data: Map<String, Any>?): Feed? {
        data ?: return null
        return try {
            Feed(
                id = (data["id"] as? Number)?.toLong() ?: return null,
                name = data["name"] as? String ?: "",
                quantityKg = (data["quantityKg"] as? Number)?.toDouble() ?: 0.0,
                cost = (data["cost"] as? Number)?.toDouble() ?: 0.0,
                lowStockThreshold = (data["lowStockThreshold"] as? Number)?.toDouble() ?: 10.0,
                lastRestocked = (data["lastRestocked"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (_: Exception) { null }
    }

    private fun taskToMap(t: Task): Map<String, Any> = mapOf(
        "id" to t.id, "title" to t.title, "description" to t.description,
        "dueDate" to t.dueDate, "isCompleted" to t.isCompleted, "createdAt" to t.createdAt
    )

    private fun mapToTask(data: Map<String, Any>?): Task? {
        data ?: return null
        return try {
            Task(
                id = (data["id"] as? Number)?.toLong() ?: return null,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                dueDate = (data["dueDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isCompleted = data["isCompleted"] as? Boolean ?: false,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (_: Exception) { null }
    }

    private fun healthToMap(r: HealthRecord): Map<String, Any> = mapOf(
        "id" to r.id, "type" to r.type, "name" to r.name, "date" to r.date,
        "cost" to r.cost, "dosage" to r.dosage, "birdsAffected" to r.birdsAffected, "notes" to r.notes
    )

    private fun mapToHealth(data: Map<String, Any>?): HealthRecord? {
        data ?: return null
        return try {
            HealthRecord(
                id = (data["id"] as? Number)?.toLong() ?: return null,
                type = data["type"] as? String ?: "Other",
                name = data["name"] as? String ?: "",
                date = (data["date"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                cost = (data["cost"] as? Number)?.toDouble() ?: 0.0,
                dosage = data["dosage"] as? String ?: "",
                birdsAffected = (data["birdsAffected"] as? Number)?.toInt() ?: 0,
                notes = data["notes"] as? String ?: ""
            )
        } catch (_: Exception) { null }
    }
}
