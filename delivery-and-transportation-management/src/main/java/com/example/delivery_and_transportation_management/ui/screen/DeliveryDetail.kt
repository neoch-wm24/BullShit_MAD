package com.example.delivery_and_transportation_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.ui.screen.Order

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetail(
    delivery: Delivery,
    navController: NavController,
    onEdit: (Delivery) -> Unit,
    onDelete: (Delivery) -> Unit,
    assignedOrders: List<Order> = emptyList() // Add this parameter
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transportation Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transport Information
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
                            "Transport Information",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            "Plate Number: ${delivery.plateNumber ?: "Not specified"}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Driver: ${delivery.driverName}")
                        Text("Type: ${delivery.type}")
                        Text("Date: ${delivery.date.takeIf { it.isNotBlank() } ?: "Not scheduled"}")
                    }
                }
            }

            // Assigned Orders Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Assigned Orders",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (assignedOrders.isNotEmpty()) {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("${assignedOrders.size} orders") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (assignedOrders.isEmpty()) {
                            Text(
                                "No orders assigned to this transport yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Orders List
            if (assignedOrders.isNotEmpty()) {
                items(assignedOrders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "Order #${order.id.take(8)}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Customer: ${order.customerName}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "📍 ${order.address}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "📦 ${order.items}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
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

            // Action Buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onEdit(delivery) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Transport")
                    }

                    if (assignedOrders.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                // Navigate to manage orders for this transport
                                navController.navigate("manage_transport_orders/${delivery.id}")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Manage Orders")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            onDelete(delivery)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete Transport")
                    }
                }
            }
        }
    }
}
