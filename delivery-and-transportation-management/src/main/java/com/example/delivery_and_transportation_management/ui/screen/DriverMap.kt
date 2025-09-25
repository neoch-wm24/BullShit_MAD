package com.example.delivery_and_transportation_management.ui.screen

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.delivery_and_transportation_management.data.Stop
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
            name = "TARUMT - Block A Ground Floor",
            address = tarumtAddress,
            location = tarumt
        )
        val combined = listOf(startPoint) + stops

        println("DriverMap OSM Debug - Route creation:")
        println("  - Start point: ${startPoint.name}")
        println("  - Delivery stops count: ${stops.size}")
        println("  - Total route points: ${combined.size}")
        println("  - Route will show lines: ${combined.size > 1}")

        combined
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
                    "🚛 OSMDroid Delivery Route Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text("📍 Start: TARUMT Block A Ground Floor")
                Text("📦 Delivery stops: ${stops.size}")
                Text("🗺️ Total route points: ${routeStops.size}")
                Text("🆓 Using FREE OpenStreetMap")

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
                        "✅ OSM Route lines: ${if (routeStops.size > 1) "ENABLED" else "DISABLED"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (routeStops.size > 1) Color.Green else Color.Red,
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
                                    val isPlaceholder = stop.address.contains("Address not available") ||
                                                       stop.address.contains("not found") ||
                                                       stop.name.contains("Unknown") ||
                                                       stop.name.contains("Customer:")
                                    if (isPlaceholder) {
                                        Text(
                                            "⚠️ Placeholder data - check Firebase",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        Text(
                                            "✅ Real receiver data (OSM)",
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
                            text = "🌐 OSM Coordinates: ${String.format("%.4f", stop.location.latitude)}, ${String.format("%.4f", stop.location.longitude)}",
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

                        // Add action buttons for delivery stops
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AssistChip(
                                    onClick = {
                                        // 打开这个特定位置的详细地图
                                        val intent = Intent(context, DriverMapActivity::class.java).apply {
                                            putExtra("lat", stop.location.latitude)
                                            putExtra("lng", stop.location.longitude)
                                            putExtra("name", stop.name)
                                            putExtra("address", stop.address)
                                        }
                                        context.startActivity(intent)
                                    },
                                    label = {
                                        Text("🗺️ View on Map", style = MaterialTheme.typography.labelSmall)
                                    }
                                )

                                AssistChip(
                                    onClick = {
                                        // 导航到这个位置
                                        val geoUri = android.net.Uri.parse(
                                            "geo:${stop.location.latitude},${stop.location.longitude}?q=${stop.name}"
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW, geoUri)
                                        context.startActivity(intent)
                                    },
                                    label = {
                                        Text("🧭 Navigate", style = MaterialTheme.typography.labelSmall)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Summary item
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
                            "📊 OSM Route Summary",
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
                                Text("🆓 Using OpenStreetMap")

                                if (stops.isEmpty()) {
                                    Text(
                                        "❌ No delivery addresses loaded",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text(
                                        "✅ OSM coordinates loaded",
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
                            Text(if (stops.isNotEmpty()) "🗺️ Open Full OSM Route Map" else "❌ No Delivery Stops")
                        }
                    }
                }
            }
        }
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
