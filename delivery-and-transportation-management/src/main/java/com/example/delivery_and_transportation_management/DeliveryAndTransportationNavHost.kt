package com.example.delivery_and_transportation_management

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.ui.screen.AddTransportationScreen
import com.example.delivery_and_transportation_management.ui.screen.DeliveryDetail
import com.example.delivery_and_transportation_management.ui.screen.EditDeliveryScreen
import androidx.compose.material3.Text
import com.example.delivery_and_transportation_management.ui.screen.DeliveryScheduleScreen
import com.example.delivery_and_transportation_management.ui.screen.SearchDeliveryandTransportationScreen
import com.example.delivery_and_transportation_management.ui.screen.AddTransportToOrderScreen

@Composable
fun DeliveryAndTransportationNavHost(
    modifier: Modifier = Modifier
) {
    val navController: NavHostController = rememberNavController()
    val deliveryViewModel: DeliveryViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "delivery_list",
        modifier = modifier
    ) {
        // Delivery list
        composable("delivery_list") {
            SearchDeliveryandTransportationScreen(
                navController = navController,
                deliveryViewModel = deliveryViewModel // Don't pass deliveries parameter - let it use ViewModel
            )
        }

        // Add new transportation
        composable("add_transportation") {
            AddTransportationScreen(
                navController = navController,
                onSave = { delivery ->
                    deliveryViewModel.addDelivery(delivery)
                    navController.popBackStack() // back to list
                }
            )
        }

        // Delivery detail
        composable("deliveryDetail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            val deliveries by deliveryViewModel.deliveries.collectAsState()
            val delivery = deliveries.find { it.id == id }

            if (delivery != null) {
                // Sample assigned orders - replace with actual data from your order management
                val sampleAssignedOrders = when (delivery.id.hashCode() % 4) {
                    0 -> emptyList()
                    1 -> listOf(
                        com.example.delivery_and_transportation_management.ui.screen.Order(
                            "ord1", "John Smith", "123 Main St", "Electronics", "High", "Pending"
                        ),
                        com.example.delivery_and_transportation_management.ui.screen.Order(
                            "ord2", "Jane Doe", "456 Oak Ave", "Books", "Medium", "Pending"
                        )
                    )
                    2 -> listOf(
                        com.example.delivery_and_transportation_management.ui.screen.Order(
                            "ord3", "Bob Wilson", "789 Pine Rd", "Furniture", "Normal", "In Progress"
                        )
                    )
                    else -> listOf(
                        com.example.delivery_and_transportation_management.ui.screen.Order(
                            "ord4", "Alice Brown", "321 Elm St", "Clothing", "High", "Pending"
                        ),
                        com.example.delivery_and_transportation_management.ui.screen.Order(
                            "ord5", "Mike Johnson", "654 Cedar Ave", "Sports Equipment", "Medium", "Ready"
                        ),
                        com.example.delivery_and_transportation_management.ui.screen.Order(
                            "ord6", "Sarah Davis", "987 Maple Dr", "Home Goods", "Normal", "Pending"
                        )
                    )
                }

                DeliveryDetail(
                    delivery = delivery,
                    navController = navController,
                    assignedOrders = sampleAssignedOrders,
                    onEdit = { toEdit ->
                        navController.navigate("editDelivery/${toEdit.id}")
                    },
                    onDelete = { toDelete ->
                        deliveryViewModel.removeDeliveries(setOf(toDelete))
                        navController.popBackStack()
                    }
                )
            } else {
                Text(text = "Delivery not found")
            }
        }

        // Edit delivery
        composable("editDelivery/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            val deliveries by deliveryViewModel.deliveries.collectAsState()
            val delivery = deliveries.find { it.id == id }

            if (delivery != null) {
                EditDeliveryScreen(
                    delivery = delivery,
                    navController = navController,
                    onSave = { updated ->
                        deliveryViewModel.updateDelivery(updated)
                        navController.popBackStack()
                    }
                )
            } else {
                Text(text = "Delivery not found")
            }
        }

        composable("delivery_schedule") {
            val deliveries by deliveryViewModel.deliveries.collectAsState()
            DeliveryScheduleScreen(
                deliveries = deliveries,
                deliveryViewModel = deliveryViewModel,
                navController = navController
            )
        }

        // New route for order assignment
        composable("assign_orders/{selectedDate}/{transportIds}") { backStackEntry ->
            val selectedDate = backStackEntry.arguments?.getString("selectedDate") ?: ""
            val transportIdsString = backStackEntry.arguments?.getString("transportIds") ?: ""
            val transportIds = transportIdsString.split(",").toSet()
            val deliveries by deliveryViewModel.deliveries.collectAsState()

            // Sample orders - you'll need to replace this with actual order data from your order management module
            val sampleOrders = listOf(
                com.example.delivery_and_transportation_management.ui.screen.Order(
                    "1", "John Doe", "123 Main St", "Electronics, Books", "High", "Pending"
                ),
                com.example.delivery_and_transportation_management.ui.screen.Order(
                    "2", "Jane Smith", "456 Oak Ave", "Clothing", "Medium", "Pending"
                ),
                com.example.delivery_and_transportation_management.ui.screen.Order(
                    "3", "Bob Johnson", "789 Pine Rd", "Furniture", "Normal", "Pending"
                )
            )

            AddTransportToOrderScreen(
                selectedDate = selectedDate,
                selectedTransportIds = transportIds,
                deliveries = deliveries,
                orders = sampleOrders, // Replace with actual orders from your order management
                navController = navController,
                onAssignOrders = { selectedOrderIds ->
                    // Actually save the order assignments to the ViewModel
                    deliveryViewModel.assignOrdersToTransports(transportIds, selectedOrderIds)
                    println("Assigned orders $selectedOrderIds to transports $transportIds for date $selectedDate")
                    // Simply pop back to the previous screen instead of complex navigation
                    navController.popBackStack()
                }
            )
        }


    }
}
