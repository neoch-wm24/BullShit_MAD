package com.example.delivery_and_transportation_management.ui.screen

import android.content.Intent
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.delivery_and_transportation_management.data.Stop
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun DriverDeliveryListScreen(
    stops: List<Stop>
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // TARUMT Ground Floor - Block A (OSMDroid GeoPoint)
    val tarumt = GeoPoint(3.2155, 101.7280)
    val tarumtAddress = "Ground Floor, Bangunan Tan Sri Khaw Kai Boh (Block A), Jalan Genting Kelang, Setapak, 53300 Kuala Lumpur, Federal Territory of Kuala Lumpur"

    // Debug: Print stops information
    LaunchedEffect(stops) {
        println("DriverMap OSM Debug - Total delivery stops received: ${stops.size}")
        stops.forEachIndexed { index, stop ->
            println("DriverMap OSM Debug - Delivery Stop $index:")
            println("  - Receiver: '${stop.name}'")
            println("  - Address: '${stop.address}'")
            println("  - OSM Coordinates: (${stop.location.latitude}, ${stop.location.longitude})")
        }

        if (stops.isEmpty()) {
            println("DriverMap OSM Debug - NO DELIVERY STOPS! Check order assignment and receiver addresses.")
        }
    }

    // Create route: Start from TARUMT + All delivery destinations
    val routeStops = remember(stops) {
        val startPoint = Stop(
            "TARUMT - Block A Ground Floor",
            tarumtAddress,
            tarumt
        )
        val combined = listOf(startPoint) + stops

        println("DriverMap OSM Debug - Route creation:")
        println("  - Start point: ${startPoint.name}")
        println("  - Delivery stops count: ${stops.size}")
        println("  - Total route points: ${combined.size}")
        println("  - Route will show lines: ${combined.size > 1}")

        combined
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
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
                    " Delivery Route Information",
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
                        "NO DELIVERY ADDRESSES FOUND",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }

        // OSMDroid Map
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(8.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    // 初始化 OSMDroid 配置
                    Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
                    Configuration.getInstance().userAgentValue = ctx.packageName

                    val mapView = MapView(ctx)
                    mapView.setTileSource(TileSourceFactory.MAPNIK) // 免费 OpenStreetMap
                    mapView.setMultiTouchControls(true)

                    // 设置初始位置和缩放
                    if (routeStops.isNotEmpty()) {
                        if (routeStops.size > 1) {
                            // 计算中心点
                            val latitudes = routeStops.map { it.location.latitude }
                            val longitudes = routeStops.map { it.location.longitude }
                            val centerLat = (latitudes.minOrNull()!! + latitudes.maxOrNull()!!) / 2
                            val centerLng = (longitudes.minOrNull()!! + longitudes.maxOrNull()!!) / 2
                            mapView.controller.setCenter(GeoPoint(centerLat, centerLng))
                            mapView.controller.setZoom(11.0)
                        } else {
                            mapView.controller.setCenter(routeStops.first().location)
                            mapView.controller.setZoom(15.0)
                        }

                        // 添加标记
                        routeStops.forEachIndexed { index, stop ->
                            val marker = Marker(mapView)
                            marker.position = stop.location
                            marker.title = if (index == 0) {
                                "🏢 START: ${stop.name}"
                            } else {
                                "📦 DELIVERY ${index}: ${stop.name}"
                            }
                            marker.snippet = "Address: ${stop.address}\nCoords: ${stop.location.latitude}, ${stop.location.longitude}"

                            // 点击标记打开详细地图
                            marker.setOnMarkerClickListener { _, _ ->
                                val intent = Intent(ctx, DriverMapActivity::class.java).apply {
                                    putExtra("lat", stop.location.latitude)
                                    putExtra("lng", stop.location.longitude)
                                    putExtra("name", stop.name)
                                    putExtra("address", stop.address)
                                }
                                ctx.startActivity(intent)
                                true
                            }

                            mapView.overlays.add(marker)
                        }

                        // 绘制路线
                        if (routeStops.size > 1) {
                            println("DriverMap OSM Debug - Drawing route lines between ${routeStops.size} points")

                            // 主路线 (红色)
                            val mainPolyline = Polyline()
                            mainPolyline.setPoints(routeStops.map { it.location })
                            mainPolyline.color = android.graphics.Color.RED
                            mainPolyline.width = 10.0f
                            mapView.overlays.add(mainPolyline)

                            // 辅助路线 (蓝色)
                            val secondaryPolyline = Polyline()
                            secondaryPolyline.setPoints(routeStops.map { it.location })
                            secondaryPolyline.color = android.graphics.Color.BLUE
                            secondaryPolyline.width = 6.0f
                            mapView.overlays.add(secondaryPolyline)

                            println("DriverMap OSM Debug - Route lines drawn successfully with OSMDroid")
                        } else {
                            println("DriverMap OSM Debug - NO LINES DRAWN: Only ${routeStops.size} point(s)")
                        }

                    } else {
                        // 默认显示 KL
                        mapView.controller.setCenter(GeoPoint(3.1390, 101.6869))
                        mapView.controller.setZoom(12.0)
                    }

                    mapView
                },
                modifier = Modifier.fillMaxSize()
            ) { mapView ->
                // 更新地图内容（如果需要的话）
                // 这里可以根据 stops 的变化来更新标记
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    // 打开完整地图显示整个路线
                    val intent = Intent(context, DriverMapActivity::class.java).apply {
                        putExtra("employeeId", "current_driver") // 需要传入当前司机ID
                        putExtra("selectedDate", "current_date") // 需要传入选中日期
                    }
                    context.startActivity(intent)
                },
                enabled = routeStops.size > 1,
                modifier = Modifier.weight(1f)
            ) {
                Text("🗺️ Open Full Map")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    // 打开外部导航应用
                    if (routeStops.size > 1) {
                        val firstDelivery = routeStops[1] // 跳过起始点
                        val gmmIntentUri = android.net.Uri.parse(
                            "google.navigation:q=${firstDelivery.location.latitude},${firstDelivery.location.longitude}"
                        )
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")

                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            // 回退到通用 geo intent
                            val geoUri = android.net.Uri.parse(
                                "geo:${firstDelivery.location.latitude},${firstDelivery.location.longitude}?q=${firstDelivery.name}"
                            )
                            val genericIntent = Intent(Intent.ACTION_VIEW, geoUri)
                            context.startActivity(genericIntent)
                        }
                    }
                },
                enabled = routeStops.size > 1,
                modifier = Modifier.weight(1f)
            ) {
                Text("🧭 Start Navigation")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Route Details List
        Text(
            "📋 Route Details (${routeStops.size} stops)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // --- Firestore + caches (moved OUTSIDE loop and keyed by receiverId) ---
        val firestore = remember { FirebaseFirestore.getInstance() }
        // Key: receiverId (document id in customers). Value: Pair(name,address)
        val customerCache = remember { mutableStateMapOf<String, Pair<String, String>? >() }
        val loadingSet = remember { mutableStateSetOf<String>() }

        fun ensureCustomer(receiverId: String, stop: Stop) {
            if (receiverId.isBlank() || receiverId == "__start_point__") return
            if (customerCache.containsKey(receiverId) || loadingSet.contains(receiverId)) return
            loadingSet.add(receiverId)
            firestore.collection("customers").document(receiverId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val name = doc.getString("name") ?: receiverId
                        val addr = doc.getString("address") ?: (stop.address.ifBlank { "Address N/A" })
                        customerCache[receiverId] = name to addr
                        loadingSet.remove(receiverId)
                    } else {
                        // fallback by field 'id'
                        firestore.collection("customers").whereEqualTo("id", receiverId).limit(1).get()
                            .addOnSuccessListener { q ->
                                val d = q.documents.firstOrNull()
                                if (d != null) {
                                    val name = d.getString("name") ?: receiverId
                                    val addr = d.getString("address") ?: (stop.address.ifBlank { "Address N/A" })
                                    customerCache[receiverId] = name to addr
                                } else {
                                    customerCache[receiverId] = null
                                }
                                loadingSet.remove(receiverId)
                            }
                            .addOnFailureListener {
                                customerCache[receiverId] = null
                                loadingSet.remove(receiverId)
                            }
                    }
                }
                .addOnFailureListener {
                    customerCache[receiverId] = null
                    loadingSet.remove(receiverId)
                }
        }

        // Route Details Cards with proper spacing
        routeStops.forEachIndexed { index, stop ->
            val receiverId = if (index == 0) "__start_point__" else stop.receiverId
            if (index > 0) {
                LaunchedEffect(receiverId) { ensureCustomer(receiverId, stop) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (index) {
                        0 -> MaterialTheme.colorScheme.primaryContainer
                        routeStops.size - 1 -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
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

                            if (index == 0) {
                                Text(
                                    text = "TARUMT Block A Ground Floor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )
                            } else {
                                val info = customerCache[receiverId]
                                val loading = loadingSet.contains(receiverId) && info == null
                                when {
                                    loading -> Text(
                                        text = "Loading recipient...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    info != null -> Text(
                                        text = info.first,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                    )
                                    else -> {
                                        // fallback: if stop.name seems more human than receiverId, show it
                                        val fallbackName = if (stop.name != receiverId) stop.name else "Recipient not found"
                                        Text(
                                            text = fallbackName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                            color = if (fallbackName == "Recipient not found") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
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

                    if (index == 0) {
                        Text(
                            text = tarumtAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        val info = customerCache[receiverId]
                        val loading = loadingSet.contains(receiverId) && info == null
                        when {
                            loading -> Text(
                                text = "Fetching address...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            info != null -> Text(
                                text = info.second,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            else -> Text(
                                text = stop.address.ifBlank { "Address unavailable" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Distance & time (unchanged logic)
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

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AssistChip(
                                onClick = {
                                    val intent = Intent(context, DriverMapActivity::class.java).apply {
                                        putExtra("lat", stop.location.latitude)
                                        putExtra("lng", stop.location.longitude)
                                        putExtra("name", customerCache[receiverId]?.first ?: if (stop.name != receiverId) stop.name else receiverId)
                                        putExtra("address", customerCache[receiverId]?.second ?: stop.address)
                                    }
                                    context.startActivity(intent)
                                },
                                label = { Text("🗺️ View on Map", style = MaterialTheme.typography.labelSmall) }
                            )
                            AssistChip(
                                onClick = {
                                    val displayName = customerCache[receiverId]?.first ?: if (stop.name != receiverId) stop.name else receiverId
                                    val geoUri = android.net.Uri.parse(
                                        "geo:${stop.location.latitude},${stop.location.longitude}?q=$displayName"
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW, geoUri)
                                    context.startActivity(intent)
                                },
                                label = { Text("🧭 Navigate", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }

        // Summary item
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Route Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Stops: ${routeStops.size}")
                        Text("Deliveries: ${stops.size}")
                    }

                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                        val totalDistance = if (routeStops.size > 1) {
                            routeStops.zipWithNext().sumOf { (from, to) ->
                                calculateDistance(from.location, to.location)
                            }
                        } else 0.0

                        Text("Total Distance: ${String.format("%.1f", totalDistance)} km")

                        val totalTime = (totalDistance / 30.0) * 60 + (stops.size * 10)
                        Text("Est. Time: ${String.format("%.0f", totalTime)} min")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val intent = Intent(context, DriverMapActivity::class.java).apply {
                            putExtra("employeeId", "current_driver")
                            putExtra("selectedDate", "current_date")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = stops.isNotEmpty()
                ) {
                    Text(if (stops.isNotEmpty()) "Open Full Route Map" else "❌ No Delivery Stops")
                }
            }
        }

        // Add bottom padding for better scrolling experience
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// OSM Distance calculation helper
private fun calculateDistance(from: GeoPoint, to: GeoPoint): Double {
    val earthRadius = 6371.0
    val latDistance = Math.toRadians(to.latitude - from.latitude)
    val lngDistance = Math.toRadians(to.longitude - from.longitude)
    val a = kotlin.math.sin(latDistance / 2) * kotlin.math.sin(latDistance / 2) +
            kotlin.math.cos(Math.toRadians(from.latitude)) * kotlin.math.cos(Math.toRadians(to.latitude)) *
            kotlin.math.sin(lngDistance / 2) * kotlin.math.sin(lngDistance / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return earthRadius * c
}
