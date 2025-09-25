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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(8.dp, bottom = if (isMultiSelectMode) 72.dp else 16.dp)
                    ) {
                        items(filteredDeliveries, key = { it.id }) { delivery ->
                            val selected = delivery in selectedItems
                            DeliveryListItem(
                                delivery = delivery,
                                selected = selected,
                                multiSelect = isMultiSelectMode,
                                navController = navController,
                                onClick = {
                                    if (isMultiSelectMode) {
                                        selectedItems = if (selected) selectedItems - delivery else selectedItems + delivery
                                    } else {
                                        navController.navigate("deliveryDetail/${delivery.id}")
                                    }
                                },
                                onAssignedOrdersClick = {
                                    if (delivery.assignedOrders.size == 1) {
                                        navController.navigate("OrderDetails/${delivery.assignedOrders.first()}")
                                    } else if (delivery.assignedOrders.isNotEmpty()) {
                                        navController.navigate("order")
                                    }
                                }
                            )
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

@Composable
private fun DeliveryListItem(
    delivery: Delivery,
    selected: Boolean,
    multiSelect: Boolean,
    navController: NavController,
    onClick: () -> Unit,
    onAssignedOrdersClick: () -> Unit
) {
    val assignedOrdersCount = delivery.assignedOrders.size
    val baseColor = Color(0xFFF8F8F8)
    val containerColor = when {
        multiSelect && selected -> MaterialTheme.colorScheme.primaryContainer
        else -> baseColor
    }

    // State for showing order selection dialog
    var showOrderDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val plateDisplay = delivery.plateNumber ?: delivery.id
                Text(
                    text = "Plate: $plateDisplay",
                    style = MaterialTheme.typography.titleMedium
                )
                if (delivery.date.isNotBlank()) {
                    Text(
                        text = delivery.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Driver: ${delivery.driverName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Type: ${delivery.type}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (assignedOrdersCount > 0) {
                Spacer(Modifier.height(6.dp))
                AssistChip(
                    onClick = {
                        when (assignedOrdersCount) {
                            1 -> {
                                // Single order - navigate directly
                                onAssignedOrdersClick()
                            }
                            else -> {
                                // Multiple orders - show dialog
                                showOrderDialog = true
                            }
                        }
                    },
                    label = {
                        Text(
                            "📦 $assignedOrdersCount ${if (assignedOrdersCount == 1) "order" else "orders"}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            } else {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "📦 No orders assigned",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    // Order selection dialog for multiple orders
    if (showOrderDialog && delivery.assignedOrders.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showOrderDialog = false },
            title = {
                Text("Select Order")
            },
            text = {
                LazyColumn {
                    items(delivery.assignedOrders) { orderId ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    showOrderDialog = false
                                    // Navigate to the specific order detail
                                    navController.navigate("OrderDetails/$orderId")
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Order #${orderId.take(12)}...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showOrderDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
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