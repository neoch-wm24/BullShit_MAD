package screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import data.Delivery
import data.DeliveryViewModel
import com.example.delivery_and_transportation_management.ui.components.ActionButtonMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreen(
    deliveries: List<Delivery>,
    onAddDelivery: () -> Unit,
    navController: NavController,
    deliveryViewModel: DeliveryViewModel = viewModel()
) {
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<Delivery>()) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter deliveries based on search query
    val filteredDeliveries = remember(searchQuery, deliveries) {
        if (searchQuery.isBlank()) {
            deliveries
        } else {
            deliveries.filter {
                it.plateNumber.contains(searchQuery, true) ||
                        it.driverName.contains(searchQuery, true) ||
                        it.type.contains(searchQuery, true) ||
                        it.date.contains(searchQuery, true)
            }
        }
    }

    Scaffold(
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search bar at the top
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    label = { Text("Search Deliveries") },
                    placeholder = { Text("Search by plate, driver, type, or date") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Multi-select toolbar
                if (isMultiSelectMode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected: ${selectedItems.size}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Row {
                                TextButton(
                                    onClick = {
                                        selectedItems = emptySet()
                                        isMultiSelectMode = false
                                    }
                                ) {
                                    Text("Cancel")
                                }
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

                // Delivery list
                if (filteredDeliveries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "No deliveries found" else "No deliveries match your search",
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
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isMultiSelectMode) {
                                            selectedItems = if (delivery in selectedItems) {
                                                selectedItems - delivery
                                            } else {
                                                selectedItems + delivery
                                            }
                                        } else {
                                            navController.navigate("deliveryDetail/${delivery.plateNumber}")
                                        }

                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMultiSelectMode && delivery in selectedItems) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
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
                                                selectedItems = if (checked) {
                                                    selectedItems + delivery
                                                } else {
                                                    selectedItems - delivery
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Plate: ${delivery.plateNumber}",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "Driver: ${delivery.driverName}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Type: ${delivery.type}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Date: ${delivery.date}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating action button menu
            ActionButtonMenu(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onToggleMultiSelect = {
                    isMultiSelectMode = !isMultiSelectMode
                    if (!isMultiSelectMode) {
                        selectedItems = emptySet()
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDeliveryScreen() {
    val sampleDeliveries = listOf(
        Delivery("ABC123", "John Doe", "Van", "2025-09-15"),
        Delivery("XYZ789", "Alice Lee", "Truck", "2025-09-16"),
        Delivery("LMN456", "Bob Tan", "Bike", "2025-09-17")
    )

    DeliveryScreen(
        deliveries = sampleDeliveries,
        onAddDelivery = {},
        navController = rememberNavController()
    )
}
