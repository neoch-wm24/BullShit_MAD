package com.example.delivery_and_transportation_management.data

import org.osmdroid.util.GeoPoint

data class Stop(
    val name: String,
    val address: String,
    val location: GeoPoint
)

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