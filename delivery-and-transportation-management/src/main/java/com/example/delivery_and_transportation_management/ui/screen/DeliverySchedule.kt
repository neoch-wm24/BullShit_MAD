package com.example.delivery_and_transportation_management.ui.screen

import android.widget.CalendarView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.viewmodel.DeliveryScheduleViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScheduleScreen(
    deliveries: List<Delivery>,
    deliveryViewModel: DeliveryViewModel,
    navController: NavController
) {
    val uiVm: DeliveryScheduleViewModel = viewModel()
    val selectedDate by uiVm.selectedDate.collectAsState()
    val selectedTransportations by uiVm.selectedTransportations.collectAsState()

    // Filter deliveries with empty assignedOrders
    val filteredDeliveries = deliveries.filter { it.assignedOrders.isEmpty() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
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
                                // If previously selected date exists, try to restore it
                                if (selectedDate.isNotBlank()) {
                                    runCatching {
                                        val parts = selectedDate.split('-')
                                        if (parts.size == 3) {
                                            val cal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, parts[0].toInt())
                                                set(Calendar.MONTH, parts[1].toInt() - 1)
                                                set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                                            }
                                            date = cal.timeInMillis
                                        }
                                    }.onFailure { date = today.timeInMillis }
                                } else {
                                    date = today.timeInMillis
                                }
                                // Set minimum date to today (prevent past dates)
                                minDate = today.timeInMillis
                                setOnDateChangeListener { _, year, month, dayOfMonth ->
                                    uiVm.setSelectedDate(
                                        String.format(
                                            Locale.getDefault(),
                                            "%04d-%02d-%02d",
                                            year,
                                            month + 1,
                                            dayOfMonth
                                        )
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

        if (filteredDeliveries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "⚠No unassigned transportation available. Go back and add some transportation first.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(filteredDeliveries) { delivery ->
                val plate = delivery.plateNumber?.takeIf { it.isNotBlank() } ?: "(No Plate)"
                val checked = selectedTransportations.contains(delivery.id)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (checked)
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
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                uiVm.setTransportationChecked(delivery.id, isChecked)
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
                            // Assume updateDeliveryDate now properly saves to Firebase
                            // Navigate to order assignment screen
                            navController.navigate("AssignOrders/$selectedDate/${selectedTransportations.joinToString(",")}")
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