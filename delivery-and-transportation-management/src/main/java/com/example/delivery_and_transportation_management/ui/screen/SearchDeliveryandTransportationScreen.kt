package com.example.delivery_and_transportation_management.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.ui.components.ActionButtonMenu
import androidx.compose.runtime.collectAsState
import com.example.core_ui.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDeliveryandTransportationScreen(
    navController: NavController,
    deliveryViewModel: DeliveryViewModel = viewModel(),
    deliveries: List<Delivery>? = null // Only for preview
) {
    // Always collect from ViewModel, but use preview data if provided
    val vmDeliveries by deliveryViewModel.deliveries.collectAsState()
    val actualDeliveries = deliveries ?: vmDeliveries

    var isMultiSelectMode by rememberSaveable { mutableStateOf(false) }
    var selectedItems by rememberSaveable { mutableStateOf(setOf<Delivery>()) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredDeliveries = remember(searchQuery, actualDeliveries) {
        if (searchQuery.isBlank()) actualDeliveries else actualDeliveries.filter {
            it.plateNumber?.contains(searchQuery, true) == true ||
                    it.driverName.contains(searchQuery, true) ||
                    it.type.contains(searchQuery, true) ||
                    it.date.contains(searchQuery, true) ||
                    it.id.contains(searchQuery, true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SearchBar(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "Search Deliveries",
                        placeholder = "plate, driver, type, date, or ID",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            bottomBar = {
                if (isMultiSelectMode) {
                    Surface(tonalElevation = 3.dp, shadowElevation = 4.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Selected: ${selectedItems.size}", style = MaterialTheme.typography.bodyLarge)

                            Row {
                                TextButton(onClick = {
                                    selectedItems = emptySet()
                                    isMultiSelectMode = false
                                }) { Text("Cancel") }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = {
                                        deliveryViewModel.removeDeliveries(selectedItems)
                                        selectedItems = emptySet()
                                        isMultiSelectMode = false
                                    },
                                    enabled = selectedItems.isNotEmpty()
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (filteredDeliveries.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isBlank()) "No deliveries found"
                            else "No deliveries match your search",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredDeliveries) { delivery ->
                            val plateDisplay = delivery.plateNumber ?: delivery.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isMultiSelectMode) {
                                            selectedItems = if (delivery in selectedItems)
                                                selectedItems - delivery
                                            else
                                                selectedItems + delivery
                                        } else {
                                            navController.navigate("deliveryDetail/${delivery.id}")
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        if (isMultiSelectMode && delivery in selectedItems)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isMultiSelectMode) {
                                        Checkbox(
                                            checked = delivery in selectedItems,
                                            onCheckedChange = { checked ->
                                                selectedItems = if (checked)
                                                    selectedItems + delivery
                                                else
                                                    selectedItems - delivery
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Plate: $plateDisplay", style = MaterialTheme.typography.titleMedium)
                                        Text("Driver: ${delivery.driverName}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Type: ${delivery.type}", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "Date: ${delivery.date.takeIf { it.isNotBlank() } ?: "Not scheduled"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        // Show assigned orders summary - use real data from Delivery
                                        val assignedOrdersCount = delivery.assignedOrders.size

                                        if (assignedOrdersCount > 0) {
                                            Row(
                                                modifier = Modifier.padding(top = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
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
                                                    label = {
                                                        Text(
                                                            "📦 $assignedOrdersCount orders",
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    },
                                                    colors = AssistChipDefaults.assistChipColors(
                                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                                    )
                                                )
                                            }
                                        } else {
                                            Text(
                                                "📦 No orders assigned",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!isMultiSelectMode) {
            ActionButtonMenu(
                navController = navController,
                onToggleMultiSelect = {
                    isMultiSelectMode = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSearchDeliveryandTransportationScreen() {
    val navController = rememberNavController()
    val fakeDeliveries = listOf(
        Delivery(id = "1", employeeID = "D001", plateNumber = "ABC123", driverName = "John Doe", type = "Van", date = "2025-09-15", stops = emptyList(), assignedOrders = emptyList()),
        Delivery(id = "2", employeeID = "D002", plateNumber = "XYZ789", driverName = "Alice Lee", type = "Truck", date = "2025-09-16", stops = emptyList(), assignedOrders = emptyList()),
        Delivery(id = "3", employeeID = "D003", plateNumber = "LMN456", driverName = "Bob Tan", type = "Bike", date = "2025-09-17", stops = emptyList(), assignedOrders = emptyList())
    )
    SearchDeliveryandTransportationScreen(
        navController = navController,
        deliveries = fakeDeliveries
    )
}