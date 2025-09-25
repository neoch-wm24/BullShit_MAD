package com.example.delivery_and_transportation_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.example.delivery_and_transportation_management.data.Stop

@Composable
fun DriverDeliveryListScreen(
    stops: List<Stop>
) {
    // TARUMT Ground Floor - Block A (updated accurate address)
    val tarumt = LatLng(3.2155, 101.7280)
    val tarumtAddress = "Ground Floor, Bangunan Tan Sri Khaw Kai Boh (Block A), Jalan Genting Kelang, Setapak, 53300 Kuala Lumpur, Federal Territory of Kuala Lumpur"

    // Debug: Print stops information to check if receiver addresses are loaded
    LaunchedEffect(stops) {
        println("DriverMap Debug - Total delivery stops received: ${stops.size}")
        stops.forEachIndexed { index, stop ->
            println("DriverMap Debug - Delivery Stop $index:")
            println("  - Receiver: '${stop.name}'")
            println("  - Address: '${stop.address}'")
            println("  - Coordinates: (${stop.location.latitude}, ${stop.location.longitude})")
        }

        if (stops.isEmpty()) {
            println("DriverMap Debug - NO DELIVERY STOPS! This means:")
            println("  - Either no orders are assigned to this delivery")
            println("  - Or receiver addresses are not being fetched from Firebase")
            println("  - Check DeliveryViewModel.assignOrderToDelivery() function")
        }
    }

    // Create route: Start from TARUMT + All delivery destinations
    val routeStops = remember(stops) {
        val startPoint = Stop(
            name = "TARUMT - Block A Ground Floor",
            address = tarumtAddress,
            location = tarumt
        )
        val combined = listOf(startPoint) + stops

        println("DriverMap Debug - Route creation:")
        println("  - Start point: ${startPoint.name}")
        println("  - Delivery stops count: ${stops.size}")
        println("  - Total route points: ${combined.size}")
        println("  - Route will show lines: ${combined.size > 1}")

        combined
    }

    // Auto-fit camera to show all route points
    val cameraPositionState = rememberCameraPositionState {
        position = if (routeStops.size > 1) {
            val latitudes = routeStops.map { it.location.latitude }
            val longitudes = routeStops.map { it.location.longitude }
            val centerLat = (latitudes.minOrNull()!! + latitudes.maxOrNull()!!) / 2
            val centerLng = (longitudes.minOrNull()!! + longitudes.maxOrNull()!!) / 2
            CameraPosition.fromLatLngZoom(LatLng(centerLat, centerLng), 11f)
        } else {
            CameraPosition.fromLatLngZoom(tarumt, 15f)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Enhanced Info Card with debug information
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (stops.isEmpty())
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "🚛 Delivery Route Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text("📍 Start: TARUMT Block A Ground Floor")
                Text("📦 Delivery stops: ${stops.size}")
                Text("🗺️ Total route points: ${routeStops.size}")

                // Debug status
                if (stops.isEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "⚠️ NO DELIVERY ADDRESSES FOUND",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        "Check if orders are assigned and receiver addresses exist in Firebase",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                } else {
                    Text(
                        "✅ Route lines: ${if (routeStops.size > 1) "ENABLED" else "DISABLED"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (routeStops.size > 1) Color.Green else Color.Red,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }

        // Enhanced Map with debug markers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(8.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.NORMAL,
                    isTrafficEnabled = true
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    myLocationButtonEnabled = false,
                    rotationGesturesEnabled = true,
                    scrollGesturesEnabled = true,
                    zoomGesturesEnabled = true,
                    tiltGesturesEnabled = true
                )
            ) {
                // Enhanced markers with better visibility
                routeStops.forEachIndexed { index, stop ->
                    Marker(
                        state = MarkerState(position = stop.location),
                        title = if (index == 0) {
                            "🏢 START: ${stop.name}"
                        } else {
                            "📦 DELIVERY ${index}: ${stop.name}"
                        },
                        snippet = "Address: ${stop.address}\nCoords: ${stop.location.latitude}, ${stop.location.longitude}"
                    )
                }

                // Enhanced route line visualization - FIXED
                if (routeStops.size > 1) {
                    println("DriverMap Debug - Drawing route lines between ${routeStops.size} points")

                    // Single polyline connecting all points
                    Polyline(
                        points = routeStops.map { it.location },
                        color = Color.Red,
                        width = 12f, // Increased width for visibility
                        pattern = null,
                        geodesic = true // Better line following Earth's curvature
                    )

                    // Secondary line for contrast
                    Polyline(
                        points = routeStops.map { it.location },
                        color = Color.Blue,
                        width = 8f,
                        geodesic = true
                    )

                    // Add segment-by-segment lines for debugging
                    routeStops.zipWithNext { from, to ->
                        Polyline(
                            points = listOf(from.location, to.location),
                            color = Color.Green,
                            width = 4f,
                            geodesic = true
                        )
                    }

                    println("DriverMap Debug - Route lines drawn successfully")
                } else {
                    println("DriverMap Debug - NO LINES DRAWN: Only ${routeStops.size} point(s)")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Route Details
        Text(
            "📋 Route Details (${routeStops.size} stops)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(routeStops) { index, stop ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (index) {
                            0 -> MaterialTheme.colorScheme.primaryContainer // Start point
                            routeStops.size - 1 -> MaterialTheme.colorScheme.secondaryContainer // Last stop
                            else -> MaterialTheme.colorScheme.surface // Regular delivery stops
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (index == 0) {
                                        "🏢 START POINT"
                                    } else {
                                        "📦 DELIVERY STOP $index"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (index == 0) {
                                        "TARUMT Block A Ground Floor"
                                    } else {
                                        "Receiver: ${stop.name}"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )

                                // Show if this is real or placeholder data
                                if (index > 0) {
                                    val isPlaceholder = stop.address.contains("Address not found") ||
                                                       stop.name.contains("Unknown")
                                    if (isPlaceholder) {
                                        Text(
                                            "⚠️ Placeholder data - check Firebase",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        Text(
                                            "✅ Real receiver data",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Green
                                        )
                                    }
                                }
                            }

                            if (index > 0) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.size(width = 40.dp, height = 24.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = androidx.compose.ui.Alignment.Center
                                    ) {
                                        Text(
                                            text = "#$index",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "📍 ${stop.address}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "🌐 Coordinates: ${String.format("%.4f", stop.location.latitude)}, ${String.format("%.4f", stop.location.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Add estimated distance for delivery stops
                        if (index > 0) {
                            val prevStop = routeStops[index - 1]
                            val distance = calculateDistance(prevStop.location, stop.location)

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "📏 Distance from previous: ~${String.format("%.1f", distance)} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )

                            val estimatedMinutes = (distance / 30.0) * 60
                            Text(
                                text = "⏱️ Estimated time: ~${String.format("%.0f", estimatedMinutes)} minutes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        // Add delivery status indicator for delivery stops
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                AssistChip(
                                    onClick = { /* TODO: Handle delivery status */ },
                                    label = {
                                        Text(
                                            "📋 Pending Delivery",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )

                                TextButton(
                                    onClick = { /* TODO: Navigate to this location */ }
                                ) {
                                    Text("🗺️ Navigate")
                                }
                            }
                        }
                    }
                }
            }

            // Add summary item at the end
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "📊 Route Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Total Stops: ${routeStops.size}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Deliveries: ${stops.size}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                if (stops.isEmpty()) {
                                    Text(
                                        "❌ No delivery addresses loaded",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text(
                                        "✅ Receiver addresses loaded",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Green
                                    )
                                }
                            }

                            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                val totalDistance = if (routeStops.size > 1) {
                                    routeStops.zipWithNext().sumOf { (from, to) ->
                                        calculateDistance(from.location, to.location)
                                    }
                                } else 0.0

                                Text(
                                    "Total Distance: ${String.format("%.1f", totalDistance)} km",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                )

                                val totalTime = (totalDistance / 30.0) * 60 + (stops.size * 10) // 10 min per stop
                                Text(
                                    "Est. Total Time: ${String.format("%.0f", totalTime)} min",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { /* TODO: Start navigation */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = stops.isNotEmpty()
                        ) {
                            Text(
                                if (stops.isNotEmpty()) "🚛 Start Route Navigation" else "❌ No Delivery Stops"
                            )
                        }
                    }
                }
            }
        }
    }
}

// Distance helper
private fun calculateDistance(from: LatLng, to: LatLng): Double {
    val earthRadius = 6371.0
    val latDistance = Math.toRadians(to.latitude - from.latitude)
    val lngDistance = Math.toRadians(to.longitude - from.longitude)
    val a = kotlin.math.sin(latDistance / 2) * kotlin.math.sin(latDistance / 2) +
            kotlin.math.cos(Math.toRadians(from.latitude)) * kotlin.math.cos(Math.toRadians(to.latitude)) *
            kotlin.math.sin(lngDistance / 2) * kotlin.math.sin(lngDistance / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return earthRadius * c
}
