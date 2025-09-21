package com.example.delivery_and_transportation_management.data

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.order_management.ui.screen.Order

// 扩展 Order 数据类，包含客户名称
data class OrderWithCustomerNames(
    val order: Order,
    val senderName: String = "Loading...",
    val receiverName: String = "Loading..."
)

class DeliveryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // deliveries
    private val _deliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val deliveries: StateFlow<List<Delivery>> = _deliveries.asStateFlow()

    // 原始订单数据
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // 👆 新增：包含客户名称的订单
    private val _ordersWithCustomerNames = MutableStateFlow<List<OrderWithCustomerNames>>(emptyList())
    val ordersWithCustomerNames: StateFlow<List<OrderWithCustomerNames>> = _ordersWithCustomerNames.asStateFlow()

    // 客户信息缓存
    private val _customers = MutableStateFlow<Map<String, String>>(emptyMap()) // ID -> Name
    val customers: StateFlow<Map<String, String>> = _customers.asStateFlow()

    // 订单分配 Map<TransportId, Set<OrderId>>
    private val _orderAssignments = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val orderAssignments: StateFlow<Map<String, Set<String>>> = _orderAssignments.asStateFlow()

    init {
        // 🔹 首先加载客户数据
        loadCustomers()

        // 实时监听 deliveries
        db.collection("deliveries")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DeliveryViewModel", "Error listening to deliveries: ${error.message}")
                    return@addSnapshotListener
                }

                val list = snapshot?.toObjects(Delivery::class.java) ?: emptyList()
                Log.d("DeliveryViewModel", "Loaded ${list.size} deliveries")
                _deliveries.value = list
            }

        // 实时监听 orders 集合
        db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DeliveryViewModel", "Error listening to orders: ${error.message}")
                    return@addSnapshotListener
                }

                val manualMappedList = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Order(
                            id = doc.getString("id") ?: doc.id,
                            senderId = doc.getString("sender_id") ?: "",
                            receiverId = doc.getString("receiver_id") ?: "",
                            parcelIds = (doc.get("parcel_ids") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            totalWeight = (doc.getDouble("total_weight") ?: 0.0),
                            cost = (doc.getDouble("cost") ?: 0.0)
                        )
                    } catch (e: Exception) {
                        Log.e("DeliveryViewModel", "Error mapping document ${doc.id}: ${e.message}")
                        null
                    }
                } ?: emptyList()

                _orders.value = manualMappedList
                Log.d("DeliveryViewModel", "Loaded ${manualMappedList.size} orders")

                // 🔹 更新带客户名称的订单列表
                updateOrdersWithCustomerNames()
            }

        // 监听订单分配
        db.collection("transportAssignments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DeliveryViewModel", "Error listening to transport assignments: ${error.message}")
                    return@addSnapshotListener
                }

                val assignments = mutableMapOf<String, Set<String>>()
                snapshot?.documents?.forEach { doc ->
                    val orderIds = (doc.get("orderIds") as? List<*>)?.mapNotNull { it as? String }?.toSet() ?: emptySet()
                    assignments[doc.id] = orderIds
                }
                _orderAssignments.value = assignments
                Log.d("DeliveryViewModel", "Loaded transport assignments: ${assignments.size}")
            }
    }

    // 🔹 加载客户数据
    private fun loadCustomers() {
        db.collection("customers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DeliveryViewModel", "Error listening to customers: ${error.message}")
                    return@addSnapshotListener
                }

                val customerMap = mutableMapOf<String, String>()
                snapshot?.documents?.forEach { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name") ?: "Unknown Customer"
                    customerMap[id] = name
                }

                _customers.value = customerMap
                Log.d("DeliveryViewModel", "Loaded ${customerMap.size} customers")

                // 🔹 客户数据更新后，重新计算订单显示
                updateOrdersWithCustomerNames()
            }
    }

    // 🔹 更新带客户名称的订单列表
    private fun updateOrdersWithCustomerNames() {
        val orders = _orders.value
        val customers = _customers.value

        if (orders.isEmpty()) {
            _ordersWithCustomerNames.value = emptyList()
            return
        }

        val ordersWithNames = orders.map { order ->
            OrderWithCustomerNames(
                order = order,
                senderName = customers[order.senderId] ?: "Customer ID: ${order.senderId}",
                receiverName = customers[order.receiverId] ?: "Customer ID: ${order.receiverId}"
            )
        }

        _ordersWithCustomerNames.value = ordersWithNames
        Log.d("DeliveryViewModel", "Updated ${ordersWithNames.size} orders with customer names")
    }

    fun addDelivery(delivery: Delivery) {
        db.collection("deliveries").document(delivery.id).set(delivery)
    }

    fun updateDelivery(updated: Delivery) {
        db.collection("deliveries").document(updated.id).set(updated)
    }

    fun updateDeliveryDate(deliveryId: String, newDate: String) {
        _deliveries.value = _deliveries.value.map {
            if (it.id == deliveryId) it.copy(date = newDate) else it
        }
    }

    fun removeDeliveries(toRemove: Set<Delivery>) {
        toRemove.forEach { delivery ->
            db.collection("deliveries").document(delivery.id).delete()
        }
        val removedIds = toRemove.map { it.id }.toSet()
        _orderAssignments.value = _orderAssignments.value.filterKeys { it !in removedIds }
    }

    fun assignOrdersToTransports(transportIds: Set<String>, orderIds: Set<String>) {
        val currentAssignments = _orderAssignments.value.toMutableMap()

        transportIds.forEach { transportId ->
            val existingOrders = currentAssignments[transportId] ?: emptySet()
            currentAssignments[transportId] = existingOrders + orderIds
        }
        _orderAssignments.value = currentAssignments

        // ✅ 同步保存到 Firestore，用 List 而不是 Set
        transportIds.forEach { transportId ->
            val list = currentAssignments[transportId]?.toList() ?: emptyList()
            db.collection("transportAssignments")
                .document(transportId)
                .set(mapOf("orderIds" to list))
        }
    }


    fun getAssignedOrdersCount(transportId: String): Int {
        return _orderAssignments.value[transportId]?.size ?: 0
    }

    fun getAssignedOrderIds(transportId: String): Set<String> {
        return _orderAssignments.value[transportId] ?: emptySet()
    }

    // 🔹 根据客户ID获取名称
    fun getCustomerName(customerId: String): String {
        return _customers.value[customerId] ?: "Customer ID: $customerId"
    }
}