package com.example.delivery_and_transportation_management.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.delivery_and_transportation_management.ui.Delivery

class DeliveryViewModel : ViewModel() {
    private val _deliveries = mutableStateListOf<Delivery>()
    val deliveries: List<Delivery> get() = _deliveries

    fun addDelivery(delivery: Delivery) {
        _deliveries.add(delivery)
    }

    fun removeDeliveries(toRemove: Set<Delivery>) {
        _deliveries.removeAll { it in toRemove }
    }
}
