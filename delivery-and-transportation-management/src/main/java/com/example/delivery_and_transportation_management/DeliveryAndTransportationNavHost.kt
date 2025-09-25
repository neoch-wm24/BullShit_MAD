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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        route = "routeMap/{driverId}/{date}",
        arguments = listOf(
            navArgument("driverId") { type = NavType.StringType },
            navArgument("date") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val driverId = backStackEntry.arguments?.getString("driverId") ?: return@composable
        val rawDate = backStackEntry.arguments?.getString("date") ?: return@composable
        val deliveries by deliveryViewModel.deliveries.collectAsState()

        // Normalize incoming date to yyyy-MM-dd (same as ViewModel canonical format)
        val canonicalFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
        val parsePatterns = listOf(
            "yyyy-MM-dd","d-M-yyyy","dd-MM-yyyy","d/MM/yyyy","dd/MM/yyyy","d-M-yy","dd-MM-yy","yyyy/M/d","yyyy/M/dd","yyyy/MM/d","yyyy/MM/dd"
        )
        fun normalize(d:String): String {
            val t = d.trim()
            if (t.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return t
            for (p in parsePatterns) {
                try { val f = SimpleDateFormat(p, Locale.getDefault()).apply { isLenient=false }; val dt:Date? = f.parse(t); if (dt!=null) return canonicalFmt.format(dt) } catch (_:Exception) {}
            }
            return t
        }
        val date = normalize(rawDate)
        // Strict filter: same driver (employeeID) AND exact normalized date AND non-blank date
        val stops = deliveries
            .filter { d ->
                val match = d.employeeID.equals(driverId, true) && d.date.isNotBlank() && d.date == date
                if (!match && d.employeeID.equals(driverId, true)) {
                    // Debug when driver matches but date not
                    println("RouteMap Debug - Skipped delivery id=${d.id} employeeID=${d.employeeID} storedDate=${d.date} requestedDate=$date")
                }
                match
            }
            .flatMap { it.stops }

        println("RouteMap Debug - driverId=$driverId requestedDate=$date -> stopsCount=${stops.size}")
        DriverDeliveryListScreen(stops = stops)
    }

    // Legacy Driver route map (without date) for backward compatibility
    composable(
        route = "routeMap/{driverId}",
        arguments = listOf(
            navArgument("driverId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val driverId = backStackEntry.arguments?.getString("driverId") ?: return@composable
        val deliveries by deliveryViewModel.deliveries.collectAsState()
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val stops = deliveries.filter { it.employeeID == driverId && it.date == today }.flatMap { it.stops }
        DriverDeliveryListScreen(stops = stops)
    }
}
