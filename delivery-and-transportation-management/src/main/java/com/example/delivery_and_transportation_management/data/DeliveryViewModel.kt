package com.example.delivery_and_transportation_management.data

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeliveryViewModel : ViewModel() {
    // Use StateFlow so UI auto-updates
    private val _deliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val deliveries: StateFlow<List<Delivery>> = _deliveries.asStateFlow()

    // Track order assignments: Map<TransportId, Set<OrderId>>
    private val _orderAssignments = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val orderAssignments: StateFlow<Map<String, Set<String>>> = _orderAssignments.asStateFlow()

    fun addDelivery(delivery: Delivery) {
        _deliveries.value = _deliveries.value + delivery
    }

    fun updateDelivery(updated: Delivery) {
        _deliveries.value = _deliveries.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    fun updateDeliveryDate(deliveryId: String, newDate: String) {
        _deliveries.value = _deliveries.value.map {
            if (it.id == deliveryId) it.copy(date = newDate) else it
        }
    }

    fun removeDeliveries(toRemove: Set<Delivery>) {
        _deliveries.value = _deliveries.value.filterNot { it in toRemove }
        // Also remove order assignments for deleted deliveries
        val removedIds = toRemove.map { it.id }.toSet()
        _orderAssignments.value = _orderAssignments.value.filterKeys { it !in removedIds }
    }

    // New function to assign orders to transports
    fun assignOrdersToTransports(transportIds: Set<String>, orderIds: Set<String>) {
        val currentAssignments = _orderAssignments.value.toMutableMap()

        transportIds.forEach { transportId ->
            val existingOrders = currentAssignments[transportId] ?: emptySet()
            currentAssignments[transportId] = existingOrders + orderIds
        }

        _orderAssignments.value = currentAssignments
    }

    // Get assigned orders count for a specific transport
    fun getAssignedOrdersCount(transportId: String): Int {
        return _orderAssignments.value[transportId]?.size ?: 0
    }

    // Get assigned order IDs for a specific transport
    fun getAssignedOrderIds(transportId: String): Set<String> {
        return _orderAssignments.value[transportId] ?: emptySet()
    }
}
