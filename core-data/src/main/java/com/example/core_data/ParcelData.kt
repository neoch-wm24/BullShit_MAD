package com.example.core_data

import java.util.UUID

// Duplicate data classes and ParcelDataStore removed. See ParcelDataModels.kt for definitions.

data class SenderInfo(val information: String)
data class RecipientInfo(val information: String)
data class ParcelInfo(
    val id: String = UUID.randomUUID().toString(),
    val information: String
)
data class AllParcelData(
    val id: String = UUID.randomUUID().toString(),
    val sender: SenderInfo,
    val recipient: RecipientInfo,
    val parcels: List<ParcelInfo>,
    val rakId: String? = null, // Add rakId to associate parcels with specific Rak
    val timestamp: Long = System.currentTimeMillis()
)

object ParcelDataStore {
    private val _orders = mutableListOf<AllParcelData>()
    val orders: List<AllParcelData> get() = _orders.toList()

    fun addOrder(order: AllParcelData) {
        _orders.add(order)
    }

    fun getOrderById(id: String): AllParcelData? {
        return _orders.find { it.id == id }
    }

    fun updateOrder(order: AllParcelData) {
        val index = _orders.indexOfFirst { it.id == order.id }
        if (index != -1) {
            _orders[index] = order
        }
    }

    fun deleteOrder(id: String) {
        _orders.removeIf { it.id == id }
    }

    fun clearOrders() { _orders.clear() }

    // Add function to get parcels for a specific Rak
    fun getOrdersForRak(rakId: String): List<AllParcelData> {
        return _orders.filter { it.rakId == rakId }
    }
}
