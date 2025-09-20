package com.example.delivery_and_transportation_management.ui.screen

import android.widget.CalendarView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScheduleScreen(
    deliveries: List<Delivery>,
    deliveryViewModel: DeliveryViewModel,
    navController: NavController
) {
    var selectedDate by rememberSaveable { mutableStateOf("") }
    var selectedTransportations by rememberSaveable { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Schedule") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calendar section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Select Date",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        AndroidView(
                            factory = { context ->
                                CalendarView(context).apply {
                                    val today = Calendar.getInstance()
                                    date = today.timeInMillis
                                    // Set minimum date to today (prevent past dates)
                                    minDate = today.timeInMillis
                                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                                        selectedDate = String.format(
                                            Locale.getDefault(),
                                            "%04d-%02d-%02d",
                                            year,
                                            month + 1,
                                            dayOfMonth
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp) // Fixed height to prevent taking all space
                        )

                        if (selectedDate.isNotBlank()) {
                            Text(
                                "Selected Date: $selectedDate",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // Transportation selection section
            item {
                Text(
                    "Select Transportation",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (deliveries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "⚠No transportation added yet. Go back and add some transportation first.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(deliveries) { delivery ->
                    val plate = delivery.plateNumber?.takeIf { it.isNotBlank() } ?: "(No Plate)"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedTransportations.contains(delivery.id))
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Checkbox(
                                checked = selectedTransportations.contains(delivery.id),
                                onCheckedChange = { checked ->
                                    selectedTransportations = if (checked)
                                        selectedTransportations + delivery.id
                                    else
                                        selectedTransportations - delivery.id
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("$plate - ${delivery.type}")
                                Text(
                                    "Driver: ${delivery.driverName}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (delivery.date.isNotBlank()) {
                                    Text(
                                        "Current Date: ${delivery.date}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                } else {
                                    Text(
                                        "No date assigned",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selection status and save button
            item {
                Column {
                    if (selectedTransportations.isNotEmpty()) {
                        Text(
                            "Selected: ${selectedTransportations.size} transportation(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedDate.isNotBlank() && selectedTransportations.isNotEmpty()) {
                                selectedTransportations.forEach { deliveryId ->
                                    deliveryViewModel.updateDeliveryDate(deliveryId, selectedDate)
                                }
                                // Navigate to order assignment screen instead of going back
                                navController.navigate("assign_orders/$selectedDate/${selectedTransportations.joinToString(",")}")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedDate.isNotBlank() && selectedTransportations.isNotEmpty()
                    ) {
                        val buttonText = when {
                            selectedDate.isBlank() -> "Select a date first"
                            selectedTransportations.isEmpty() -> "Select transportation first"
                            else -> "Continue to Assign Orders (${selectedTransportations.size} transports)"
                        }
                        Text(buttonText)
                    }
                }
            }
        }
    }
}
