package com.example.delivery_and_transportation_management.ui.screen

import android.preference.PreferenceManager
import android.widget.CalendarView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.data.Stop
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DriverHome(
    navController: NavController,
    employeeID: String,
    deliveryViewModel: DeliveryViewModel = viewModel()
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Demo data 使用 OSMDroid GeoPoint
    val demoDeliveries = listOf(
        Delivery(
            id = "demo-delivery-1",
            employeeID = employeeID,
            driverName = "John Doe",
            type = "Grocery",
            date = today,
            plateNumber = "ABC123",
            stops = listOf(
                Stop("Alice Johnson", "1 Raffles Place, Singapore 048616", GeoPoint(1.2844, 103.8511)),
                Stop("Bob Smith", "Marina Bay Sands, 10 Bayfront Ave, Singapore 018956", GeoPoint(1.2834, 103.8607)),
                Stop("Charlie Brown", "Gardens by the Bay, 18 Marina Gardens Dr, Singapore 018953", GeoPoint(1.2816, 103.8636)),
                Stop("Diana Lee", "Singapore Flyer, 30 Raffles Ave, Singapore 039803", GeoPoint(1.2897, 103.8634))
            ),
            assignedOrders = listOf("ORD001", "ORD002", "ORD003")
        )
    )

    // Collect deliveries from StateFlow
    val deliveries by deliveryViewModel.deliveries.collectAsState()

    // Added: normalized employee id
    val normalizedEmployeeId = remember(employeeID) { employeeID.trim() }

    var selectedDate by remember { mutableStateOf(today) }
    var showCalendar by remember { mutableStateOf(false) }
    var showRoute by remember { mutableStateOf(false) }
    var selectedStop by remember { mutableStateOf<Stop?>(null) }

    // Debug: Enhanced logging for September 25th issue
    LaunchedEffect(deliveries, employeeID, selectedDate) {
        println("DriverHome Debug - employeeID: $employeeID")
        println("DriverHome Debug - selectedDate: $selectedDate")
        println("DriverHome Debug - today: $today")
        println("DriverHome Debug - Total deliveries from DB: ${deliveries.size}")

        if (deliveries.isEmpty()) {
            println("DriverHome Debug - NO DELIVERIES FOUND IN DATABASE!")
            println("DriverHome Debug - Check Firestore collection 'deliveries'")
        } else {
            println("DriverHome Debug - All deliveries in database:")
            deliveries.forEachIndexed { index, delivery ->
                println("DriverHome Debug - Delivery $index: id='${delivery.id}', employeeID='${delivery.employeeID}', driverName='${delivery.driverName}', date='${delivery.date}', plateNumber='${delivery.plateNumber}', orders=${delivery.assignedOrders.size}")
            }
        }

        val mine = deliveries.filter { it.employeeID.equals(employeeID, true) }
        println("DriverHome Debug - Deliveries for employeeID $employeeID: ${mine.size}")

        if (mine.isEmpty()) {
            val available = deliveries.map { it.employeeID }.distinct().filter { it.isNotBlank() }
            println("DriverHome Debug - Available employeeIDs: $available")
        } else {
            mine.forEach { d -> println("DriverHome Debug - Driver's delivery: id=${d.id}, date='${d.date}', plateNumber='${d.plateNumber}'") }
        }
    }

    // REPLACED actualDeliveries with enhanced matching (driverId OR delivery.id)
    val actualDeliveries = remember(deliveries, normalizedEmployeeId) {
        deliveries.filter { it.employeeID.trim().equals(normalizedEmployeeId, ignoreCase = true) }
            .also { list ->
                if (list.isEmpty()) {
                    println("DriverHome Match Debug - NO deliveries matched employeeID '$normalizedEmployeeId'. Listing all loaded deliveries:")
                    deliveries.forEachIndexed { index, d ->
                        println("DriverHome Match Debug - [$index] id='${d.id}', employeeID='${d.employeeID}', date='${d.date}', orders=${d.assignedOrders.size}")
                    }
                } else {
                    println("DriverHome Match Debug - Found ${list.size} deliveries for employeeID '$normalizedEmployeeId'")
                }
            }
    }

    // STRICT date filtering: only exact yyyy-MM-dd match (dates already normalized in ViewModel)
    val currentDateDeliveries = remember(actualDeliveries, selectedDate) {
        val filtered = actualDeliveries.filter { it.date == selectedDate }
        println("DriverHome Date Filter - selectedDate=$selectedDate -> ${filtered.size} matches (strict)")
        if (filtered.isEmpty() && actualDeliveries.isNotEmpty()) {
            println("DriverHome Date Filter Debug - Available dates for this driver: " + actualDeliveries.map { it.date }.distinct())
        }
        filtered
    }

    val uiDeliveries = when {
        // Show real deliveries if found
        currentDateDeliveries.isNotEmpty() -> {
            println("DriverHome Debug - Showing ${currentDateDeliveries.size} real deliveries")
            currentDateDeliveries
        }
        // Show demo data for today if no real data
        selectedDate == today -> {
            println("DriverHome Debug - Showing demo data for today")
            demoDeliveries
        }
        // Show empty for other dates
        else -> {
            println("DriverHome Debug - Showing empty list for $selectedDate")
            emptyList()
        }
    }

    val completedCount = uiDeliveries.count { it.assignedOrders.isNotEmpty() }
    val incompleteCount = uiDeliveries.size - completedCount

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top statistics cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard("Today's Tasks", uiDeliveries.size, Modifier.weight(1f))
                StatCard("Completed", completedCount, Modifier.weight(1f))
                StatCard("Incomplete", incompleteCount, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date selection
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showCalendar = !showCalendar }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Date")
                }
                Text(
                    text = selectedDate,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            WeekRow(selectedDate) { newDate ->
                selectedDate = newDate
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Deliveries list
            Text("Deliveries for $selectedDate", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            if (uiDeliveries.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No deliveries found for $selectedDate",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Check console for debug information",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiDeliveries) { delivery ->
                        DeliveryCard(delivery) {
                            if (delivery.stops.isNotEmpty()) {
                                selectedStop = delivery.stops.first()
                                showRoute = true
                            }
                        }
                    }
                }
            }

            // Start delivery route button
            if (selectedDate == today && uiDeliveries.isNotEmpty()) {
                Button(
                    onClick = {
                        navController.navigate("routeMap/$employeeID/$selectedDate")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Start Delivery Route")
                }
            }

            // OSMDroid route display (替换 Google Maps 的部分)
            if (showRoute && uiDeliveries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Today's OSM Route", style = MaterialTheme.typography.titleLarge)

                val stops = uiDeliveries.flatMap { it.stops }
                if (stops.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
                                Configuration.getInstance().userAgentValue = ctx.packageName

                                val mapView = MapView(ctx)
                                mapView.setTileSource(TileSourceFactory.MAPNIK)
                                mapView.setMultiTouchControls(true)

                                if (stops.isNotEmpty()) {
                                    // 设置中心点
                                    mapView.controller.setCenter(stops.first().location)
                                    mapView.controller.setZoom(12.0)

                                    // 添加标记
                                    stops.forEach { stop ->
                                        val marker = Marker(mapView)
                                        marker.position = stop.location
                                        marker.title = stop.name
                                        marker.snippet = stop.address
                                        mapView.overlays.add(marker)
                                    }

                                    // 绘制路线
                                    if (stops.size > 1) {
                                        val polyline = Polyline()
                                        polyline.setPoints(stops.map { it.location })
                                        polyline.color = android.graphics.Color.BLUE
                                        polyline.width = 8.0f
                                        mapView.overlays.add(polyline)
                                    }
                                }

                                mapView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Calendar overlay
        if (showCalendar) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        CalendarView(context).apply {
                            setOnDateChangeListener { _, year, month, dayOfMonth ->
                                val cal = Calendar.getInstance()
                                cal.set(year, month, dayOfMonth)
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                selectedDate = sdf.format(cal.time)
                                showCalendar = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Stop details dialog
    if (selectedStop != null) {
        val stop = selectedStop
        AlertDialog(
            onDismissRequest = { selectedStop = null },
            confirmButton = {
                TextButton(onClick = { selectedStop = null }) {
                    Text("Close")
                }
            },
            title = { Text("Recipient Details") },
            text = {
                Column {
                    Text("Name: ${stop?.name ?: ""}")
                    Text("Address: ${stop?.address ?: ""}")
                }
            }
        )
    }
}

@Composable
fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun DeliveryCard(delivery: Delivery, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with delivery ID and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Delivery #${delivery.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = delivery.plateNumber ?: "No Vehicle Assigned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status badge
                val statusColor = if (delivery.assignedOrders.isNotEmpty())
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                val statusText = if (delivery.assignedOrders.isNotEmpty())
                    "ACTIVE" else "PENDING"

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = statusColor.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Information grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoRow(
                        label = "Driver",
                        value = delivery.driverName,
                        icon = "👤"
                    )
                    InfoRow(
                        label = "Vehicle Type",
                        value = delivery.type,
                        icon = "🚛"
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    InfoRow(
                        label = "Scheduled Date",
                        value = if (delivery.date.isNotBlank()) delivery.date else "Not Scheduled",
                        icon = "📅"
                    )
                    InfoRow(
                        label = "Stops",
                        value = "${delivery.stops.size} locations",
                        icon = "📍"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Orders section
            if (delivery.assignedOrders.isNotEmpty()) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            "📦 ${delivery.assignedOrders.size} ${if (delivery.assignedOrders.size == 1) "order" else "orders"} assigned",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Order details
                Column {
                    delivery.assignedOrders.take(3).forEach { orderId ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(16.dp)
                            )
                            Text(
                                "Order #${orderId.take(8)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (delivery.assignedOrders.size > 3) {
                        Text(
                            "   +${delivery.assignedOrders.size - 3} more orders...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", modifier = Modifier.padding(end = 8.dp))
                        Text(
                            "No orders assigned to this delivery",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Delivery locations preview
            if (delivery.stops.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Delivery Locations:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                delivery.stops.take(2).forEach { stop ->
                    Row(
                        modifier = Modifier.padding(vertical = 1.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("📍", modifier = Modifier.padding(end = 4.dp))
                        Column {
                            Text(
                                stop.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                stop.address,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
                if (delivery.stops.size > 2) {
                    Text(
                        "   +${delivery.stops.size - 2} more locations...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(end = 4.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun WeekRow(selectedDate: String, onDateSelected: (String) -> Unit) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.time = sdf.parse(selectedDate) ?: Date()

    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val daysOfWeek = (0..6).map {
        val date = sdf.format(cal.time)
        val label = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
        cal.add(Calendar.DAY_OF_MONTH, 1)
        label to date
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        daysOfWeek.forEach { (label, date) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onDateSelected(date) }
            ) {
                Text(label ?: "", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = date.substring(8),
                    style = if (date == selectedDate)
                        MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
                    else MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
