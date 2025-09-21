package com.example.core_data

import androidx.compose.runtime.mutableStateListOf
import java.util.Date

// Data classes for parcel management
data class ParcelInfo(
    val id: String,
    val description: String,
    val weight: String,
    val dimensions: String,
    val value: String,
    val information: String = "$description - Weight: $weight, Size: $dimensions, Value: $value"
)

data class AddressInfo(
    val name: String,
    val phone: String,
    val addressLine: String,
    val city: String,
    val postalCode: String,
    val state: String,
    val information: String = "$name, $phone, $addressLine, $city $postalCode, $state"
)

data class AllParcelData(
    val id: String,
    val timestamp: Date,
    val sender: AddressInfo,
    val recipient: AddressInfo,
    val parcels: List<ParcelInfo>,
    val status: String = "In-Stock" // Add status field: "In-Stock", "Out-Stock"
)

// Global state manager for parcel data
object ParcelDataManager {
    private val _allParcelData = mutableStateListOf<AllParcelData>()
    val allParcelData: List<AllParcelData> get() = _allParcelData.toList()

    fun addOrder(orderData: AllParcelData) {
        _allParcelData.add(orderData)
    }

    fun removeOrder(orderId: String) {
        _allParcelData.removeAll { it.id == orderId }
    }

    // New method to update order status instead of removing
    fun updateOrderStatus(orderId: String, newStatus: String) {
        val index = _allParcelData.indexOfFirst { it.id == orderId }
        if (index != -1) {
            val currentOrder = _allParcelData[index]
            _allParcelData[index] = currentOrder.copy(status = newStatus)
        }
    }

    // Get only in-stock orders for display in RackInformation
    fun getInStockOrders(): List<AllParcelData> {
        return _allParcelData.filter { it.status == "In-Stock" }
    }

    fun getOrderById(id: String): AllParcelData? {
        return _allParcelData.find { it.id == id }
    }

    fun getAllOrders(): List<AllParcelData> {
        return _allParcelData.toList()
    }

    fun clearAllOrders() {
        _allParcelData.clear()
    }

    // Save order using generic data structure (called from AddOrderandParcel screen)
    fun saveOrderFromUI(
        orderId: String,
        senderName: String,
        senderPhone: String,
        senderAddressLine: String,
        senderCity: String,
        senderPostalCode: String,
        senderState: String,
        receiverName: String,
        receiverPhone: String,
        receiverAddressLine: String,
        receiverCity: String,
        receiverPostalCode: String,
        receiverState: String,
        parcels: List<ParcelInfo>
    ) {
        val senderInfo = AddressInfo(
            name = senderName,
            phone = senderPhone,
            addressLine = senderAddressLine,
            city = senderCity,
            postalCode = senderPostalCode,
            state = senderState
        )

        val receiverInfo = AddressInfo(
            name = receiverName,
            phone = receiverPhone,
            addressLine = receiverAddressLine,
            city = receiverCity,
            postalCode = receiverPostalCode,
            state = receiverState
        )

        val orderData = AllParcelData(
            id = orderId,
            timestamp = Date(),
            sender = senderInfo,
            recipient = receiverInfo,
            parcels = parcels,
            status = "In-Stock" // Default status when adding new order
        )

        addOrder(orderData)
    }
}
