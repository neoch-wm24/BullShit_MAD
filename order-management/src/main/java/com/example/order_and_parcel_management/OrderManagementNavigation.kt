package com.example.order_and_parcel_management

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.order_and_parcel_management.ui.screen.AddOrderScreen
import com.example.order_and_parcel_management.ui.screen.OrderDetailScreen
import com.example.order_and_parcel_management.ui.screen.SearchOrderAndParcelScreen
import com.example.order_and_parcel_management.ui.screen.SelectOrderandParcelScreen

fun NavGraphBuilder.orderNavigation(navController: NavHostController) {
    // Order 相关页面
    composable("order") {
        SearchOrderAndParcelScreen(
            navController = navController,
            modifier = Modifier
        )
    }

    composable(
        "OrderDetails/{orderId}",
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        OrderDetailScreen(orderId = orderId)
    }

    composable("multiple_select") {
        SelectOrderandParcelScreen(
            navController = navController,
        )
    }

    composable("AddOrder") {
        AddOrderScreen(
            navController = navController,
            modifier = Modifier
        )
    }
}