package com.example.delivery_and_transportation_management.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.Delivery
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.delivery_and_transportation_management.viewmodel.DeliveryDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetail(
    delivery: Delivery,
    navController: NavController,
    onEdit: (Delivery) -> Unit,
    onDelete: (Delivery) -> Unit
) {
    val detailVm: DeliveryDetailViewModel = viewModel() // retained UI holder (future expansion)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .then(if (isLandscape) Modifier.verticalScroll(rememberScrollState()) else Modifier),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top: Transport details (similar placement style to AddTransportation)
        TransportInfoCard(delivery = delivery)

        // Assigned Orders Summary - match search screen style
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Assigned Orders",
                        style = MaterialTheme.typography.titleMedium
                    )
                    val assignedOrdersCount = delivery.assignedOrders.size
                    if (assignedOrdersCount > 0) {
                        AssistChip(
                            onClick = {
                                // Navigate to order list or first order if only one
                                if (delivery.assignedOrders.size == 1) {
                                    navController.navigate("OrderDetails/${delivery.assignedOrders.first()}")
                                } else {
                                    // For multiple orders, navigate to the general order screen for now
                                    // TODO: Implement a dedicated screen for delivery orders
                                    navController.navigate("order")
                                }
                            },
                            label = { Text("\uD83D\uDCE6 $assignedOrdersCount orders") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (delivery.assignedOrders.isEmpty()) {
                    Text(
                        "No orders assigned to this transport yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Action Buttons
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
            OutlinedButton(
                onClick = {
                    val selectedDate = delivery.date.ifBlank { "unscheduled" }
                    navController.navigate("AssignOrders/$selectedDate/${delivery.id}")
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Manage Orders") }
            OutlinedButton(
                onClick = { onDelete(delivery) },
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

@Composable
private fun TransportInfoCard(delivery: Delivery) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
