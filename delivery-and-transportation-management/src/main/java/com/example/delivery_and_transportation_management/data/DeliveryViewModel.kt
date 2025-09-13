package com.example.delivery_and_transportation_management.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class DeliveryViewModel : ViewModel() {
    private val _deliveries = mutableStateListOf<Delivery>()
    val deliveries: List<Delivery> get() = _deliveries

    fun addDelivery(delivery: Delivery) {
        _deliveries.add(delivery)
    }

    fun removeDeliveries(toRemove: Set<Delivery>) {
        _deliveries.removeAll { it in toRemove }
    }

    fun updateDelivery(updated: Delivery) {
        val index = _deliveries.indexOfFirst { it.plateNumber == updated.plateNumber }
        if (index != -1) {
            _deliveries[index] = updated
        }
    }
}