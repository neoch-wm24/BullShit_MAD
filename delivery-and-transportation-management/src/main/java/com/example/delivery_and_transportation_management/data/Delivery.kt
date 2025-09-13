package com.example.delivery_and_transportation_management.data

data class Delivery(
    val id: String,
    val driverName: String,
    val type: String,   // e.g. Car, Van, Air, Sea
    val date: String,   // delivery date
    val plateNumber: String? = null // optional if not all deliveries have vehicles
)
