package com.example.delivery_and_transportation_management

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.ui.screen.AddTransportationScreen
import com.example.delivery_and_transportation_management.ui.screen.DeliveryDetail
import com.example.delivery_and_transportation_management.ui.screen.DeliveryScreen
import com.example.delivery_and_transportation_management.ui.screen.EditDeliveryScreen
import androidx.compose.material3.Text

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
            DeliveryScreen(
                deliveries = deliveryViewModel.deliveries,
                navController = navController,
                onAddDelivery = { navController.navigate("add_transportation") },
                onDeleteSelected = { selected ->
                    selected.forEach { deliveryViewModel.removeDelivery(it) }
                }
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
            val delivery = deliveryViewModel.deliveries.find { it.id == id }

            if (delivery != null) {
                DeliveryDetail(
                    delivery = delivery,
                    navController = navController,
                    onEdit = { toEdit ->
                        navController.navigate("editDelivery/${toEdit.id}")
                    },
                    onDelete = { toDelete ->
                        deliveryViewModel.removeDelivery(toDelete)
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
            val delivery = deliveryViewModel.deliveries.find { it.id == id }

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
    }
}
