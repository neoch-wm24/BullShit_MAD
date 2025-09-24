package com.example.core_data

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.util.Date

data class ParcelInfo(
    val id: String = "",
    val description: String = "",
    val weight: String = "",
    val dimensions: String = "",
    val value: String = "",
    val information: String = "$description - Weight: $weight, Size: $dimensions, Value: $value"
)

data class AddressInfo(
    val name: String = "",
    val phone: String = "",
    val addressLine: String = "",
    val city: String = "",
    val postalCode: String = "",
    val state: String = "",
    val information: String = "$name, $phone, $addressLine, $city $postalCode, $state"
)

data class AllParcelData(
    val id: String = "",
    val timestamp: Date = Date(),
    val sender: AddressInfo = AddressInfo(),
    val recipient: AddressInfo = AddressInfo(),
    val parcels: List<ParcelInfo> = emptyList(),
    val status: String = "In-Stock",
    val rackId: String = ""
)

object ParcelDataManager {
    private val db = FirebaseFirestore.getInstance()
    private val _allParcelData = mutableStateListOf<AllParcelData>()
    val allParcelData: List<AllParcelData> get() = _allParcelData.toList()

    private var listener: ListenerRegistration? = null

    init {
        // Firestore 实时监听 orders 集合
        listener = db.collection("orders")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    _allParcelData.clear()
                    _allParcelData.addAll(snapshot.toObjects(AllParcelData::class.java))
                }
            }
    }

    suspend fun addOrder(orderData: AllParcelData) {
        val col = db.collection("orders")
        val useAutoId = orderData.id.isBlank() || orderData.id.contains('/')
        val docRef = if (useAutoId) col.document() else col.document(orderData.id)
        docRef.set(orderData).await()
    }

    suspend fun removeOrder(orderId: String) {
        val col = db.collection("orders")
        if (orderId.isBlank() || orderId.contains('/')) {
            val snapshot = col.whereEqualTo("id", orderId).get().await()
            snapshot.documents.forEach { it.reference.delete().await() }
        } else {
            col.document(orderId).delete().await()
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        val col = db.collection("orders")
        if (orderId.isBlank() || orderId.contains('/')) {
            val snapshot = col.whereEqualTo("id", orderId).get().await()
            snapshot.documents.forEach { it.reference.update("status", newStatus).await() }
        } else {
            col.document(orderId).update("status", newStatus).await()
        }
    }

    fun getInStockOrders(): List<AllParcelData> {
        return _allParcelData.filter { it.status == "In-Stock" }
    }

    fun getOrdersByRack(rackId: String): List<AllParcelData> {
        if (rackId.isBlank()) return emptyList()
        return _allParcelData.filter { it.status == "In-Stock" && it.rackId == rackId }
    }

    fun getOrderById(id: String): AllParcelData? {
        return _allParcelData.find { it.id == id }
    }

    fun clearAllOrders() {
        _allParcelData.clear()
    }

    suspend fun saveOrderFromUI(
        orderId: String,
        sender: AddressInfo,
        receiver: AddressInfo,
        parcels: List<ParcelInfo>,
        rackId: String = ""
    ) {
        val orderData = AllParcelData(
            id = orderId,
            timestamp = Date(),
            sender = sender,
            recipient = receiver,
            parcels = parcels,
            status = "In-Stock",
            rackId = rackId
        )
        addOrder(orderData)
    }
}