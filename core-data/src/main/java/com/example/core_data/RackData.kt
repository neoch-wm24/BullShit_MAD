package com.example.core_data

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

data class RackInfo(
    val id: String = "",
    val name: String = "",
    val layer: Int = 0,
    val state: String = "Idle"
)

object RackManager {
    private val db = FirebaseFirestore.getInstance()
    private val _rackList = mutableStateListOf<RackInfo>()
    val rackList: List<RackInfo> get() = _rackList.toList()

    private var currentRack: String = ""
    private var listener: ListenerRegistration? = null

    init {
        // Firestore 实时监听 racks 集合
        listener = db.collection("racks")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    _rackList.clear()
                    _rackList.addAll(snapshot.toObjects(RackInfo::class.java))
                }
            }
    }

    suspend fun addRack(rack: RackInfo) {
        db.collection("racks").document(rack.id).set(rack).await()
    }

    suspend fun removeRack(id: String) {
        db.collection("racks").document(id).delete().await()
    }

    fun getRackNames(): List<String> {
        return _rackList.map { it.name }
    }

    fun getRackById(id: String): RackInfo? {
        return _rackList.find { it.id == id }
    }

    fun setCurrentRack(rack: String) {
        currentRack = rack
    }

    fun getCurrentRack(): String = currentRack

    fun clearCurrentRack() {
        currentRack = ""
    }
}