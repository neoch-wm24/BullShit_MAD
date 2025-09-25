package com.example.delivery_and_transportation_management.data

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.order_management.ui.screen.Order
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

// 扩展 Order 数据类，包含客户名称
data class OrderWithCustomerNames(
    val order: Order,
    val senderName: String = "Loading...",
    val receiverName: String = "Loading..."
)

class DeliveryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _deliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val deliveries: StateFlow<List<Delivery>> = _deliveries.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _ordersWithCustomerNames = MutableStateFlow<List<OrderWithCustomerNames>>(emptyList())
    val ordersWithCustomerNames: StateFlow<List<OrderWithCustomerNames>> = _ordersWithCustomerNames.asStateFlow()

    private val _customers = MutableStateFlow<Map<String, String>>(emptyMap())
    val customers: StateFlow<Map<String, String>> = _customers.asStateFlow()

    private val canonicalDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
    private val parsePatterns = listOf(
        "yyyy-MM-dd",
        "d-M-yyyy",
        "dd-MM-yyyy",
        "d/MM/yyyy",
        "dd/MM/yyyy",
        "d-M-yy",
        "dd-MM-yy",
        "yyyy/M/d",
        "yyyy/M/dd",
        "yyyy/MM/d",
        "yyyy/MM/dd"
    )

    private fun normalizeDate(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        // Already canonical
        if (trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return trimmed
        for (p in parsePatterns) {
            try {
                val f = SimpleDateFormat(p, Locale.getDefault()).apply { isLenient = false }
                val date: Date = f.parse(trimmed) ?: continue
                return canonicalDateFormat.format(date)
            } catch (_: Exception) { }
        }
        return trimmed // fallback (won't match unless user picks same raw string)
    }

    init {
        loadCustomers()

        db.collection("deliveries")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DeliveryViewModel", "Error listening to deliveries: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    _deliveries.value = emptyList()
                    return@addSnapshotListener
                }
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        val rawDocId = doc.id
                        val id = (doc.getString("id") ?: rawDocId)
                        val employeeIdValue = (doc.getString("employeeID")
                            ?: doc.getString("driverId")
                            ?: doc.getString("driver_id")
                            ?: "").trim().ifBlank { rawDocId } // final fallback
                        val driverName = doc.getString("driverName") ?: doc.getString("name") ?: ""
                        val rawDate = doc.getString("date") ?: ""
                        val normalized = normalizeDate(rawDate)
                        // Auto-fix Firestore date if normalization changed it (non-blocking)
                        if (rawDate.isNotBlank() && normalized != rawDate) {
                            doc.reference.update("date", normalized)
                        }
                        val plateNumber = doc.getString("plateNumber") ?: doc.getString("plate_number")
                        val type = doc.getString("type") ?: ""
                        val assignedOrders = (doc.get("assignedOrders") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        val stopsRaw = doc.get("stops")
                        val stops = when (stopsRaw) {
                            is List<*> -> stopsRaw.mapNotNull { stopAny ->
                                try {
                                    val map = stopAny as? Map<*, *> ?: return@mapNotNull null
                                    val name = map["name"] as? String ?: ""
                                    val address = map["address"] as? String ?: ""
                                    val locationAny = map["location"]
                                    val latLng = when (locationAny) {
                                        is GeoPoint -> com.google.android.gms.maps.model.LatLng(locationAny.latitude, locationAny.longitude)
                                        is Map<*, *> -> {
                                            val lat = (locationAny["latitude"] ?: locationAny["lat"]) as? Double ?: 0.0
                                            val lng = (locationAny["longitude"] ?: locationAny["lng"]) as? Double ?: 0.0
                                            com.google.android.gms.maps.model.LatLng(lat, lng)
                                        }
                                        else -> com.google.android.gms.maps.model.LatLng(0.0,0.0)
                                    }
                                    Stop(name,address,latLng)
                                } catch (e: Exception) { null }
                            }
                            else -> emptyList()
                        }
                        Delivery(
                            id = id,
                            employeeID = employeeIdValue,
                            driverName = driverName,
                            type = type,
                            date = normalized,
                            plateNumber = plateNumber,
                            stops = stops,
                            assignedOrders = assignedOrders
                        )
                    } catch (e: Exception) {
                        Log.e("DeliveryViewModel","Mapping error doc ${doc.id}: ${e.message}")
                        null
                    }
                }
                _deliveries.value = list
            }

        db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { Log.e("DeliveryViewModel","Error listening to orders: ${error.message}"); return@addSnapshotListener }
                val manualMappedList = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Order(
                            id = doc.getString("id") ?: doc.id,
                            senderId = doc.getString("sender_id") ?: "",
                            receiverId = doc.getString("receiver_id") ?: "",
                            parcelIds = (doc.get("parcel_ids") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            totalWeight = doc.getDouble("total_weight") ?: 0.0,
                            cost = doc.getDouble("cost") ?: 0.0
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                _orders.value = manualMappedList
                updateOrdersWithCustomerNames()
            }
    }

    private fun loadCustomers() {
        db.collection("customers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { Log.e("DeliveryViewModel","Error listening to customers: ${error.message}"); return@addSnapshotListener }
                val map = snapshot?.documents?.associate { d ->
                    val id = d.getString("id") ?: d.id
                    val name = d.getString("name") ?: "Unknown Customer"
                    id to name
                } ?: emptyMap()
                _customers.value = map
                updateOrdersWithCustomerNames()
            }
    }

    private fun updateOrdersWithCustomerNames() {
        val customers = _customers.value
        _ordersWithCustomerNames.value = _orders.value.map { o ->
            OrderWithCustomerNames(
                order = o,
                senderName = customers[o.senderId] ?: "Customer ID: ${o.senderId}",
                receiverName = customers[o.receiverId] ?: "Customer ID: ${o.receiverId}"
            )
        }
    }

    private fun deliveryToMap(delivery: Delivery): Map<String, Any?> = mapOf(
        "id" to delivery.id,
        // Store both for backward compatibility
        "employeeID" to delivery.employeeID,
        "driverId" to delivery.employeeID,
        "driverName" to delivery.driverName,
        "type" to delivery.type,
        "date" to normalizeDate(delivery.date),
        "plateNumber" to delivery.plateNumber,
        "assignedOrders" to delivery.assignedOrders,
        "stops" to delivery.stops.map { stop ->
            mapOf(
                "name" to stop.name,
                "address" to stop.address,
                "location" to mapOf(
                    "latitude" to stop.location.latitude,
                    "longitude" to stop.location.longitude
                )
            )
        }
    )

    fun addDelivery(delivery: Delivery) {
        val canon = delivery.copy(date = normalizeDate(delivery.date))
        db.collection("deliveries").document(canon.id)
            .set(deliveryToMap(canon))
            .addOnFailureListener { e -> Log.e("DeliveryViewModel","Error adding delivery: ${e.message}") }
    }

    fun updateDelivery(updated: Delivery) {
        val canon = updated.copy(date = normalizeDate(updated.date))
        db.collection("deliveries").document(canon.id)
            .update(deliveryToMap(canon))
            .addOnFailureListener { e -> Log.e("DeliveryViewModel","Error updating delivery: ${e.message}") }
    }

    fun updateDeliveryDate(deliveryId: String, newDate: String) {
        val target = _deliveries.value.find { it.id == deliveryId } ?: return
        val canonDate = normalizeDate(newDate)
        updateDelivery(target.copy(date = canonDate))
    }

    fun removeDeliveries(toRemove: Set<Delivery>) {
        toRemove.forEach { d ->
            db.collection("deliveries").document(d.id).delete()
        }
    }

    fun assignOrderToDelivery(deliveryId: String, orderId: String) {
        val target = _deliveries.value.find { it.id == deliveryId } ?: return
        if (orderId in target.assignedOrders) return

        // Get the order to find receiver information
        val order = _orders.value.find { it.id == orderId }
        if (order == null) {
            Log.w("DeliveryViewModel", "Order $orderId not found when assigning to delivery")
            println("DeliveryViewModel Debug - Order $orderId not found in orders list")
            return
        }

        println("DeliveryViewModel Debug - Assigning order $orderId to delivery $deliveryId")
        println("DeliveryViewModel Debug - Order receiver ID: ${order.receiverId}")

        // Create a new stop for this order's receiver
        db.collection("customers").document(order.receiverId).get()
            .addOnSuccessListener { customerDoc ->
                println("DeliveryViewModel Debug - Customer document fetch successful for ${order.receiverId}")

                if (customerDoc.exists()) {
                    println("DeliveryViewModel Debug - Customer document exists. All fields: ${customerDoc.data}")

                    val receiverName = customerDoc.getString("name") ?: "Unknown Receiver"

                    // Try multiple possible address field names
                    val receiverAddress = customerDoc.getString("address")
                        ?: customerDoc.getString("Address")
                        ?: customerDoc.getString("location")
                        ?: customerDoc.getString("Location")
                        ?: customerDoc.getString("addr")
                        ?: customerDoc.getString("street")
                        ?: customerDoc.getString("full_address")
                        ?: "Address not available"

                    println("DeliveryViewModel Debug - Customer data retrieved:")
                    println("  - Name: '$receiverName'")
                    println("  - Address: '$receiverAddress'")

                    // Check if we found a valid address
                    if (receiverAddress == "Address not available") {
                        println("DeliveryViewModel Debug - WARNING: No address field found for customer ${order.receiverId}")
                        println("DeliveryViewModel Debug - Available fields: ${customerDoc.data?.keys}")
                        println("DeliveryViewModel Debug - You may need to add an 'address' field to your customer documents")
                    }

                    // Create coordinates for the address
                    // Using more realistic coordinates around KL area
                    val baseLatKL = 3.1390
                    val baseLngKL = 101.6869
                    val randomOffset = 0.03 // Smaller offset for more realistic spread
                    val randomLat = baseLatKL + (Math.random() - 0.5) * randomOffset
                    val randomLng = baseLngKL + (Math.random() - 0.5) * randomOffset

                    val newStop = Stop(
                        name = receiverName,
                        address = receiverAddress,
                        location = com.google.android.gms.maps.model.LatLng(randomLat, randomLng)
                    )

                    println("DeliveryViewModel Debug - Created stop:")
                    println("  - Name: '${newStop.name}'")
                    println("  - Address: '${newStop.address}'")
                    println("  - Location: (${newStop.location.latitude}, ${newStop.location.longitude})")

                    // Check if this stop already exists (same receiver)
                    val existingStops = target.stops.toMutableList()
                    val stopExists = existingStops.any { it.name == receiverName }

                    if (!stopExists) {
                        existingStops.add(newStop)
                        println("DeliveryViewModel Debug - Added stop to delivery. Total stops: ${existingStops.size}")
                    } else {
                        println("DeliveryViewModel Debug - Stop for receiver '$receiverName' already exists, not adding duplicate")
                    }

                    // Update delivery with new order and stop
                    val updatedDelivery = target.copy(
                        assignedOrders = target.assignedOrders + orderId,
                        stops = existingStops
                    )

                    println("DeliveryViewModel Debug - Updating delivery:")
                    println("  - Total assigned orders: ${updatedDelivery.assignedOrders.size}")
                    println("  - Total stops: ${updatedDelivery.stops.size}")

                    updateDelivery(updatedDelivery)
                    Log.d("DeliveryViewModel", "Order $orderId assigned to delivery $deliveryId with receiver stop")
                } else {
                    Log.w("DeliveryViewModel", "Customer ${order.receiverId} not found in Firebase")
                    println("DeliveryViewModel Debug - Customer document ${order.receiverId} does not exist in customers collection!")

                    // Create a placeholder stop with the receiver ID as name
                    val placeholderStop = Stop(
                        name = "Customer: ${order.receiverId}",
                        address = "Customer address not found in database",
                        location = com.google.android.gms.maps.model.LatLng(3.1390 + Math.random() * 0.01, 101.6869 + Math.random() * 0.01)
                    )

                    val existingStops = target.stops.toMutableList()
                    existingStops.add(placeholderStop)

                    val updatedDelivery = target.copy(
                        assignedOrders = target.assignedOrders + orderId,
                        stops = existingStops
                    )
                    updateDelivery(updatedDelivery)
                }
            }
            .addOnFailureListener { e ->
                Log.e("DeliveryViewModel", "Error fetching customer ${order.receiverId}: ${e.message}")
                println("DeliveryViewModel Debug - Failed to fetch customer ${order.receiverId}: ${e.message}")

                // Create a placeholder stop even on failure
                val errorStop = Stop(
                    name = "Customer: ${order.receiverId}",
                    address = "Error loading customer data: ${e.message}",
                    location = com.google.android.gms.maps.model.LatLng(3.1390 + Math.random() * 0.01, 101.6869 + Math.random() * 0.01)
                )

                val existingStops = target.stops.toMutableList()
                existingStops.add(errorStop)

                val updatedDelivery = target.copy(
                    assignedOrders = target.assignedOrders + orderId,
                    stops = existingStops
                )
                updateDelivery(updatedDelivery)
            }
    }

    fun assignOrdersToDeliveries(deliveryIds: Set<String>, orderIds: Set<String>) {
        val dList = deliveryIds.toList()
        if (dList.isEmpty()) return
        orderIds.toList().forEachIndexed { index, orderId ->
            val deliveryId = dList[index % dList.size]
            assignOrderToDelivery(deliveryId, orderId)
        }
    }

    fun getAssignedOrdersCount(deliveryId: String): Int =
        _deliveries.value.find { it.id == deliveryId }?.assignedOrders?.size ?: 0

    fun getAssignedOrderIds(deliveryId: String): Set<String> =
        _deliveries.value.find { it.id == deliveryId }?.assignedOrders?.toSet() ?: emptySet()

    fun getAllAssignedOrderIds(): Set<String> =
        _deliveries.value.flatMap { it.assignedOrders }.toSet()

    fun getCustomerName(customerId: String): String =
        _customers.value[customerId] ?: "Customer ID: $customerId"
}
