package com.example.delivery_and_transportation_management.ui.screen

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
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.data.DeliveryViewModel

// Data class for Order (you might need to adjust this based on your order structure)
data class Order(
    val id: String,
    val customerName: String,
    val address: String,
    val items: String,
    val priority: String = "Normal",
    val status: String = "Pending"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransportToOrderScreen(
    selectedDate: String,
    selectedTransportIds: Set<String>,
    deliveries: List<Delivery>,
    orders: List<Order>, // You'll need to pass this from your order management
    navController: NavController,
    onAssignOrders: (Set<String>) -> Unit // Callback to assign selected orders
) {
    var selectedOrders by rememberSaveable { mutableStateOf(setOf<String>()) }

    // Get the selected transports for display
    val selectedTransports = deliveries.filter { it.id in selectedTransportIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assign Orders to Transport") },
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
            // Show selected date and transports
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Delivery Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            "Date: $selectedDate",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Transports: ${selectedTransports.size} selected",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Show selected transports
                        selectedTransports.forEach { transport ->
                            val plate = transport.plateNumber?.takeIf { it.isNotBlank() } ?: "(No Plate)"
                            Text(
                                "• $plate - ${transport.type} (${transport.driverName})",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Orders selection section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Select Orders to Assign",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (selectedOrders.isNotEmpty()) {
                        Text(
                            "Selected: ${selectedOrders.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (orders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "No orders available. Go to order management to create some orders first.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(orders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedOrders.contains(order.id))
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Checkbox(
                                checked = selectedOrders.contains(order.id),
                                onCheckedChange = { checked ->
                                    selectedOrders = if (checked) {
                                        selectedOrders + order.id
                                    } else {
                                        selectedOrders - order.id
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Order #${order.id.take(8)}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Customer: ${order.customerName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Address: ${order.address}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Items: ${order.items}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    // Priority badge
                                    AssistChip(
                                        onClick = { },
                                        label = {
                                            Text(
                                                order.priority,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = when (order.priority) {
                                                "High" -> MaterialTheme.colorScheme.errorContainer
                                                "Medium" -> MaterialTheme.colorScheme.tertiaryContainer
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                    )
                                    // Status badge
                                    AssistChip(
                                        onClick = { },
                                        label = {
                                            Text(
                                                order.status,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (selectedOrders.isNotEmpty()) {
                                onAssignOrders(selectedOrders)
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedOrders.isNotEmpty()
                    ) {
                        val buttonText = if (selectedOrders.isEmpty()) {
                            "Select orders first"
                        } else {
                            "Assign ${selectedOrders.size} Order(s) to Transport"
                        }
                        Text(buttonText)
                    }

                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

// Preview function for testing
@Composable
fun PreviewAddTransportToOrderScreen() {
    val sampleOrders = listOf(
        Order("1", "John Doe", "123 Main St", "Electronics, Books", "High", "Pending"),
        Order("2", "Jane Smith", "456 Oak Ave", "Clothing", "Medium", "Pending"),
        Order("3", "Bob Johnson", "789 Pine Rd", "Furniture", "Normal", "Pending")
    )

    val sampleDeliveries = listOf(
        com.example.delivery_and_transportation_management.data.Delivery(
            "d1", "Driver A", "Van", "2025-09-20", "ABC123"
        )
    )

    AddTransportToOrderScreen(
        selectedDate = "2025-09-20",
        selectedTransportIds = setOf("d1"),
        deliveries = sampleDeliveries,
        orders = sampleOrders,
        navController = androidx.navigation.compose.rememberNavController(),
        onAssignOrders = { }
    )
}