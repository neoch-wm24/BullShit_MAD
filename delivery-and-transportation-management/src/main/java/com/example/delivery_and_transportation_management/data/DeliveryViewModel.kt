package com.example.delivery_and_transportation_management.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class DeliveryViewModel : ViewModel() {
    private val _deliveries = mutableStateListOf<Delivery>()
    val deliveries: List<Delivery> get() = _deliveries

    fun addDelivery(delivery: Delivery) {
        _deliveries.add(delivery)
    }

    fun updateDelivery(updated: Delivery) {
        val index = _deliveries.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            _deliveries[index] = updated // ✅ in-place update (preserves list + keeps state)
        }
    }

    fun removeDelivery(delivery: Delivery) {
        _deliveries.remove(delivery)
    }

    // ✅ New function: only update date by ID
    fun updateDeliveryDate(deliveryId: String, newDate: String) {
        val index = _deliveries.indexOfFirst { it.id == deliveryId }
        if (index != -1) {
            val existing = _deliveries[index]
            _deliveries[index] = existing.copy(date = newDate)
        }
    }
}
