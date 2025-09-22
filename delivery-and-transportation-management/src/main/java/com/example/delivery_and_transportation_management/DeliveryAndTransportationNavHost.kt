package com.example.delivery_and_transportation_management

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.material3.Text
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.ui.screen.*

fun NavGraphBuilder.deliveryAndTransportationNavigation(
    navController: NavHostController,
    deliveryViewModel: DeliveryViewModel
) {
    // Delivery list
    composable("delivery") {
        SearchDeliveryandTransportationScreen(
            navController = navController,
            deliveryViewModel = deliveryViewModel
        )
    }

    // Add new transportation
    composable("AddTransportation") {
        AddTransportationScreen(
            navController = navController,
            deliveryViewModel = deliveryViewModel // 👈 添加 ViewModel 参数
        )
    }

    // Delivery detail - navigated from SearchDeliveryandTransportationScreen
    composable(
        route = "deliveryDetail/{id}",
        arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getString("id") ?: return@composable
        val deliveries by deliveryViewModel.deliveries.collectAsState()
        val delivery = deliveries.find { it.id == id }

        if (delivery != null) {
            DeliveryDetail(
                delivery = delivery,
                navController = navController,
                onEdit = { toEdit -> navController.navigate("Edit_Delivery/${toEdit.id}") },
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
    composable(
        route = "Edit_Delivery/{id}",
        arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) { backStackEntry ->
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

    // Schedule
    composable("Delivery_Schedule") {
        val deliveries by deliveryViewModel.deliveries.collectAsState()
        DeliveryScheduleScreen(
            deliveries = deliveries,
            deliveryViewModel = deliveryViewModel,
            navController = navController
        )
    }

    // Assign orders
    composable(
        "AssignOrders/{selectedDate}/{transportIds}",
        arguments = listOf(
            navArgument("selectedDate") { type = NavType.StringType },
            navArgument("transportIds") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val selectedDate = backStackEntry.arguments?.getString("selectedDate") ?: ""
        val transportIdsString = backStackEntry.arguments?.getString("transportIds") ?: ""
        val transportIds = transportIdsString.split(",").toSet()

        val deliveries by deliveryViewModel.deliveries.collectAsState()
        val ordersWithNames by deliveryViewModel.ordersWithCustomerNames.collectAsState()

        AddTransportToOrderScreen(
            selectedDate = selectedDate,
            selectedTransportIds = transportIds,
            deliveries = deliveries,
            ordersWithNames = ordersWithNames,
            navController = navController,
            deliveryViewModel = deliveryViewModel, // 👈 添加 ViewModel 参数
            onAssignOrders = { selectedOrderIds ->
                // 👇 修复：使用正确的交付方法名
                deliveryViewModel.assignOrdersToDeliveries(transportIds, selectedOrderIds)
                navController.popBackStack()
            }
        )
    }

    // Driver route map
    composable(
        route = "routeMap/{driverId}",
        arguments = listOf(navArgument("driverId") { type = NavType.StringType })
    ) { backStackEntry ->
        val driverId = backStackEntry.arguments?.getString("driverId") ?: return@composable
        val deliveries by deliveryViewModel.deliveries.collectAsState()
        val stops = deliveries.filter { it.id == driverId }.flatMap { it.stops }
        DriverDeliveryListScreen(
            stops = stops,
            onCheckout = { navController.popBackStack() }
        )
    }
}
