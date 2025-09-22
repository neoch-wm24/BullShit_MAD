package com.example.delivery_and_transportation_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.data.Stop
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDeliveryListScreen(
    navController: NavController,
    employeeID: String,
    selectedDate: String? = null,
    deliveryViewModel: DeliveryViewModel = viewModel(),
    onShowMap: (() -> Unit)? = null
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val deliveries by deliveryViewModel.deliveries.collectAsState()
    val actualSelectedDate = selectedDate ?: today
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Demo data for testing
    val demoDeliveries = listOf(
        com.example.delivery_and_transportation_management.data.Delivery(
            id = employeeID,
            driverName = "John Doe",
            type = "Grocery",
            date = actualSelectedDate,
            plateNumber = "ABC123",
            stops = listOf(
                Stop("Alice Johnson", "123 Main Street, Downtown, City 12345", com.google.android.gms.maps.model.LatLng(3.139, 101.6869)),
                Stop("Bob Wilson", "456 Oak Avenue, Uptown, City 54321", com.google.android.gms.maps.model.LatLng(3.0738, 101.5183)),
                Stop("Carol Smith", "789 Pine Road, Suburb, City 67890", com.google.android.gms.maps.model.LatLng(3.1478, 101.7000))
            )
        )
    )

    val actualDeliveries = if (deliveries.isEmpty()) demoDeliveries else deliveries.filter { it.id == employeeID }
    val todayDeliveries = actualDeliveries.filter { it.date == actualSelectedDate }
    val allStops = todayDeliveries.flatMap { it.stops }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Route - $actualSelectedDate") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (allStops.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { onShowMap?.invoke() ?: navController.navigate("routeMap/$employeeID?selectedDate=$actualSelectedDate") }) {
                            Text("View Map")
                        }
                        Button(onClick = {
                            Toast.makeText(context, "All deliveries completed!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }) {
                            Text("Check Out")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (allStops.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No deliveries for $actualSelectedDate",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Go Back")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Today's Deliveries",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${allStops.size} stops to complete",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                items(allStops.withIndex().toList()) { (index, stop) ->
                    DeliveryStopCard(
                        stop = stop,
                        stopNumber = index + 1,
                        onCopyAddress = { address ->
                            clipboardManager.setText(AnnotatedString(address))
                            Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DeliveryStopCard(
    stop: Stop,
    stopNumber: Int,
    onCopyAddress: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = stopNumber.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stop.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Delivery Stop",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Address:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stop.address,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { onCopyAddress(stop.address) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy Address",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}