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
        // ✅ REMOVED: No longer listen to "orders" collection
        // We only use "racks" collection now
    }

    // ✅ Get orders from racks collection for a specific rack
    suspend fun getOrdersFromRack(rackId: String): List<AllParcelData> {
        return try {
            val snapshot = db.collection("racks")
                .document(rackId)
                .collection("orders")
                .get()
                .await()

            snapshot.toObjects(AllParcelData::class.java)
                .map { it.copy(rackId = rackId) } // Ensure rackId is set
                .filter { it.status != "Out-Stock" } // Filter out out-stocked orders
        } catch (e: Exception) {
            println("Failed to get orders from rack $rackId: ${e.message}")
            emptyList()
        }
    }

    // ✅ Get all orders from all racks
    suspend fun getAllOrdersFromRacks(): List<AllParcelData> {
        return try {
            val allOrders = mutableListOf<AllParcelData>()
            val racksSnapshot = db.collection("racks").get().await()

            for (rackDoc in racksSnapshot.documents) {
                val rackId = rackDoc.id
                val ordersSnapshot = db.collection("racks")
                    .document(rackId)
                    .collection("orders")
                    .get()
                    .await()

                val rackOrders = ordersSnapshot.toObjects(AllParcelData::class.java)
                    .map { it.copy(rackId = rackId) }

                allOrders.addAll(rackOrders)
            }

            allOrders
        } catch (e: Exception) {
            println("Failed to get all orders from racks: ${e.message}")
            emptyList()
        }
    }

    // ✅ 写入 racks/{rackId}/orders
    suspend fun addOrder(rackId: String, orderData: AllParcelData) {
        db.collection("racks")
            .document(rackId)
            .collection("orders")
            .document(orderData.id)
            .set(orderData.copy(rackId = rackId))
            .await()
    }

    // ✅ 删除
    suspend fun removeOrder(rackId: String, orderId: String) {
        db.collection("racks")
            .document(rackId)
            .collection("orders")
            .document(orderId)
            .delete()
            .await()
    }

    // ✅ 更新状态
    suspend fun updateOrderStatus(rackId: String, orderId: String, newStatus: String) {
        db.collection("racks")
            .document(rackId)
            .collection("orders")
            .document(orderId)
            .update("status", newStatus)
            .await()
    }

    fun getInStockOrders(): List<AllParcelData> {
        return _allParcelData.filter { it.status == "In-Stock" }
    }

    fun getOrdersByRack(rackId: String): List<AllParcelData> {
        return _allParcelData.filter { it.rackId == rackId }
    }

    fun getOrderById(id: String): AllParcelData? {
        return _allParcelData.find { it.id == id }
    }

    fun clearAllOrders() {
        _allParcelData.clear()
    }

    // ✅ 从 UI 保存
    suspend fun saveOrderFromUI(
        orderId: String,
        sender: AddressInfo,
        receiver: AddressInfo,
        parcels: List<ParcelInfo>,
        rackId: String
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
        addOrder(rackId, orderData)
    }
}