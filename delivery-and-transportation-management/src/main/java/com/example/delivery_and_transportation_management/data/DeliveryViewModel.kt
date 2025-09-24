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

    init {
        // 🔹 首先加载客户数据
        loadCustomers()

        // 实时监听 deliveries
        db.collection("deliveries")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DeliveryViewModel", "Error listening to deliveries: ${error.message}")
                    println("DeliveryViewModel Firebase Error: ${error.message}")
                    return@addSnapshotListener
                }

                val list = snapshot?.toObjects(Delivery::class.java) ?: emptyList()
                Log.d("DeliveryViewModel", "Loaded ${list.size} deliveries")
                println("DeliveryViewModel Firebase Debug - Loaded ${list.size} deliveries from Firebase")

                // Enhanced debugging for each delivery
                list.forEachIndexed { index, delivery ->
                    println("DeliveryViewModel Firebase Debug - Delivery $index: id='${delivery.id}', driverId='${delivery.driverId}', driverName='${delivery.driverName}', date='${delivery.date}', plateNumber='${delivery.plateNumber}', assignedOrders=${delivery.assignedOrders.size}")
                }

                if (list.isEmpty()) {
                    println("DeliveryViewModel Firebase Debug - NO DELIVERIES FOUND IN FIREBASE!")
                    println("DeliveryViewModel Firebase Debug - Check if:")
                    println("DeliveryViewModel Firebase Debug - 1. Deliveries collection exists in Firestore")
                    println("DeliveryViewModel Firebase Debug - 2. Data was saved correctly")
                    println("DeliveryViewModel Firebase Debug - 3. Firebase rules allow reading")
                }

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
        // ✅ 保存到 Firestore，包括 assignedOrders 字段
        db.collection("deliveries").document(delivery.id).set(delivery)
            .addOnSuccessListener {
                Log.d("DeliveryViewModel", "Delivery added successfully: ${delivery.id}")
            }
            .addOnFailureListener { e ->
                Log.e("DeliveryViewModel", "Error adding delivery: ${e.message}")
            }
    }

    fun updateDelivery(updated: Delivery) {
        // ✅ 保存更新后的 Delivery，包括 date 和 assignedOrders 字段
        db.collection("deliveries").document(updated.id).set(updated)
            .addOnSuccessListener {
                Log.d("DeliveryViewModel", "Delivery updated successfully: ${updated.id}")
            }
            .addOnFailureListener { e ->
                Log.e("DeliveryViewModel", "Error updating delivery: ${e.message}")
            }
    }

    fun updateDeliveryDate(deliveryId: String, newDate: String) {
        // ✅ 找到对应 delivery 并更新日期，同时保存到 Firestore
        val currentDeliveries = _deliveries.value
        val deliveryToUpdate = currentDeliveries.find { it.id == deliveryId }

        if (deliveryToUpdate != null) {
            val updatedDelivery = deliveryToUpdate.copy(date = newDate)
            updateDelivery(updatedDelivery)
        } else {
            Log.w("DeliveryViewModel", "Delivery not found: $deliveryId")
        }
    }

    fun removeDeliveries(toRemove: Set<Delivery>) {
        toRemove.forEach { delivery ->
            db.collection("deliveries").document(delivery.id).delete()
                .addOnSuccessListener {
                    Log.d("DeliveryViewModel", "Delivery removed: ${delivery.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("DeliveryViewModel", "Error removing delivery: ${e.message}")
                }
        }
    }

    // ✅ 新增：将单个订单分配给某个 delivery
    fun assignOrderToDelivery(deliveryId: String, orderId: String) {
        val currentDeliveries = _deliveries.value
        val deliveryToUpdate = currentDeliveries.find { it.id == deliveryId }

        if (deliveryToUpdate != null) {
            val currentAssignedOrders = deliveryToUpdate.assignedOrders.toMutableList()
            if (!currentAssignedOrders.contains(orderId)) {
                currentAssignedOrders.add(orderId)
                val updatedDelivery = deliveryToUpdate.copy(assignedOrders = currentAssignedOrders)
                updateDelivery(updatedDelivery)
                Log.d("DeliveryViewModel", "Order $orderId assigned to delivery ${deliveryId}")
            } else {
                Log.w("DeliveryViewModel", "Order $orderId already assigned to delivery ${deliveryId}")
            }
        } else {
            Log.w("DeliveryViewModel", "Delivery not found: $deliveryId")
        }
    }

    // ✅ 新增：将多个订单分配给多个 deliveries（轮询分配）
    fun assignOrdersToDeliveries(deliveryIds: Set<String>, orderIds: Set<String>) {
        val deliveryList = deliveryIds.toList()
        val ordersList = orderIds.toList()
        val numDeliveries = deliveryList.size

        ordersList.forEachIndexed { index, orderId ->
            val deliveryId = deliveryList[index % numDeliveries]
            assignOrderToDelivery(deliveryId, orderId)
        }
    }

    // ✅ 更新：从 delivery 内部获取已分配订单数量
    fun getAssignedOrdersCount(deliveryId: String): Int {
        return _deliveries.value.find { it.id == deliveryId }?.assignedOrders?.size ?: 0
    }

    // ✅ 更新：从 delivery 内部获取已分配的订单ID
    fun getAssignedOrderIds(deliveryId: String): Set<String> {
        return _deliveries.value.find { it.id == deliveryId }?.assignedOrders?.toSet() ?: emptySet()
    }

    // 🔹 获取所有已分配的订单ID（用于过滤未分配订单）
    fun getAllAssignedOrderIds(): Set<String> {
        return _deliveries.value.flatMap { it.assignedOrders }.toSet()
    }

    // 🔹 根据客户ID获取名称
    fun getCustomerName(customerId: String): String {
        return _customers.value[customerId] ?: "Customer ID: $customerId"
    }
}