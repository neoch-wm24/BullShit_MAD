package com.example.delivery_and_transportation_management.ui.screen

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.data.Stop
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DriverHome(
    navController: NavController,
    employeeID: String,
    deliveryViewModel: DeliveryViewModel = viewModel()
) {
    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // 示例数据（包含 stops）
    val demoDeliveries = listOf(
        Delivery(
            id = employeeID,
            driverName = "John Doe",
            type = "Grocery",
            date = today,
            plateNumber = "ABC123",
            stops = listOf(
                Stop("Alice", "123 Street A", com.google.android.gms.maps.model.LatLng(3.139, 101.6869)),
                Stop("Bob", "456 Street B", com.google.android.gms.maps.model.LatLng(3.0738, 101.5183))
            )
        )
    )

    // Collect deliveries from StateFlow
    val deliveries by deliveryViewModel.deliveries.collectAsState()
    val actualDeliveries = if (deliveries.isEmpty()) demoDeliveries else deliveries.filter { it.id == employeeID }

    var selectedDate by remember { mutableStateOf(today) }
    var showCalendar by remember { mutableStateOf(false) }
    var showRoute by remember { mutableStateOf(false) }
    var selectedStop by remember { mutableStateOf<Stop?>(null) }

    val currentDeliveries = actualDeliveries.filter { it.date == selectedDate }
    val completedCount = currentDeliveries.count { it.plateNumber != null }
    val incompleteCount = currentDeliveries.size - completedCount

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ===== 顶部三格统计 =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard("Today's Tasks", currentDeliveries.size, Modifier.weight(1f))
                StatCard("Completed", completedCount, Modifier.weight(1f))
                StatCard("Incomplete", incompleteCount, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== 日期选择 =====
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

            // ===== 配送记录 =====
            Text("Deliveries for $selectedDate", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(currentDeliveries) { delivery ->
                    DeliveryCard(delivery) {
                        if (delivery.stops.isNotEmpty()) {
                            selectedStop = delivery.stops.first() // 默认点第一个
                        }
                    }
                }
            }

// ===== 当天才有按钮 =====
            if (selectedDate == today && currentDeliveries.isNotEmpty()) {
                Button(
                    onClick = {
                        navController.navigate("routeMap/$employeeID")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Start Delivery Route")
                }
            }

            // ===== 内嵌路线展示 =====
            if (showRoute && currentDeliveries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Today's Route", style = MaterialTheme.typography.titleLarge)

                val stops = currentDeliveries.flatMap { it.stops }
                if (stops.isNotEmpty()) {
                    val cameraPositionState = rememberCameraPositionState {
                        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(stops.first().location, 12f)
                    }

                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        cameraPositionState = cameraPositionState
                    ) {
                        stops.forEach { stop ->
                            Marker(
                                state = MarkerState(position = stop.location),
                                title = stop.name,
                                snippet = stop.address,
                                onClick = {
                                    selectedStop = stop
                                    false
                                }
                            )
                        }
                        if (stops.size > 1) {
                            Polyline(
                                points = stops.map { it.location },
                                color = Color.Blue, // ✅ 改成这样
                                width = 8f
                            )
                        }
                    }
                }
            }
        }

        // ===== 覆盖式日历 =====
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

    // ===== Stop 详情 Dialog =====
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
                style = MaterialTheme.typography.labelSmall // ✅ 改小
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
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text("Delivery ID: ${delivery.id}", style = MaterialTheme.typography.bodyMedium)
            Text("Driver: ${delivery.driverName}", style = MaterialTheme.typography.bodySmall)
            Text("Type: ${delivery.type}", style = MaterialTheme.typography.bodySmall)
            Text("Date: ${delivery.date}", style = MaterialTheme.typography.bodySmall)
            delivery.plateNumber?.let {
                Text("Vehicle: $it", style = MaterialTheme.typography.bodySmall)
            }
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
                Text(label, style = MaterialTheme.typography.bodySmall)
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

// 工具函数: 获取相对日期
fun getRelativeDate(offset: Int): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, offset)
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(cal.time)
}
