package com.example.delivery_and_transportation_management.data

import org.osmdroid.util.GeoPoint

data class Stop(
    val receiverId: String,   // 从 orders.receiver_id 拿
    val name: String = "",    // Firestore 查询出来后再替换
    val address: String = "",
    val location: GeoPoint
) {
    // Backwards compatibility: old code used Stop(name, address, location)
    constructor(name: String, address: String, location: GeoPoint) : this(
        receiverId = name, // fallback: treat provided name as receiverId
        name = name,
        address = address,
        location = location
    )
}


data class Delivery(
    val id: String = "",              // unique delivery id
    val employeeID: String = "",   // ✅ 新增字段
    val driverName: String = "",
    val type: String = "",   // e.g. Car, Van, Air, Sea
    val date: String = "",   // delivery date
    val plateNumber: String? = null, // optional if not all deliveries have vehicles
    val stops: List<Stop> = emptyList(),
    val assignedOrders: List<String> = emptyList() // 👈 分配的订单ID列表
)