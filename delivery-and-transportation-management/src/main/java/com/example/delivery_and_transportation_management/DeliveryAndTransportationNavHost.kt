package com.example.delivery_and_transportation_management

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.ui.screen.AddTransportationScreen
import com.example.delivery_and_transportation_management.ui.screen.DeliveryDetail
import com.example.delivery_and_transportation_management.ui.screen.DeliveryScreen
import com.example.delivery_and_transportation_management.ui.screen.EditDeliveryScreen

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
        composable("delivery_list") {
            DeliveryScreen(
                deliveries = deliveryViewModel.deliveries,
                onAddDelivery = {
                    navController.navigate("add_transportation")
                },
                navController = navController,
                deliveryViewModel = deliveryViewModel
            )
        }

        composable("add_transportation") {
            AddTransportationScreen(
                navController = navController,
                onSave = { delivery ->
                    deliveryViewModel.addDelivery(delivery)
                }
            )
        }
        composable("deliveryDetail/{plateNumber}") { backStackEntry ->
            val plateNumber = backStackEntry.arguments?.getString("plateNumber") ?: return@composable
            val deliveryViewModel: DeliveryViewModel = viewModel()
            val delivery = deliveryViewModel.deliveries.find { it.plateNumber == plateNumber }

            if (delivery != null) {
                DeliveryDetail(
                    delivery = delivery,
                    navController = navController,
                    onEdit = { updatedDelivery ->
                        navController.navigate("editDelivery/${updatedDelivery.plateNumber}")
                    }
                )
            }
        }

        composable("editDelivery/{plateNumber}") { backStackEntry ->
            val plateNumber = backStackEntry.arguments?.getString("plateNumber") ?: return@composable
            val deliveryViewModel: DeliveryViewModel = viewModel()
            val delivery = deliveryViewModel.deliveries.find { it.plateNumber == plateNumber }

            if (delivery != null) {
                EditDeliveryScreen(
                    delivery = delivery,
                    navController = navController,
                    onSave = { updated ->
                        deliveryViewModel.updateDelivery(updated)
                    }
                )
            }
        }

    }
}
