package com.example.delivery_and_transportation_management.data

import com.google.android.gms.maps.model.LatLng

data class Stop(
    val name: String,
    val address: String,
    val location: LatLng
)

data class Delivery(
    val id: String = "",
    val driverName: String = "",
    val type: String = "",   // e.g. Car, Van, Air, Sea
    val date: String = "",   // delivery date
    val plateNumber: String? = null, // optional if not all deliveries have vehicles
    val stops: List<Stop> = emptyList() // Add stops property
)
