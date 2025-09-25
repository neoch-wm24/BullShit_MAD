package com.example.warehouse_management

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.warehouse_management.ui.screen.InStockScreen
import com.example.warehouse_management.ui.screen.SearchRackScreen
import com.example.warehouse_management.ui.screen.AddRackScreen
import com.example.warehouse_management.ui.screen.RackInformationScreen
import com.example.warehouse_management.ui.screen.OutStockScreen

fun NavGraphBuilder.warehouseNavigation(navController: NavHostController) {
    // 仓库主界面（搜索货架）
    composable("warehouse") {
        SearchRackScreen(
            navController = navController
        )
    }

    // 货架详情
    composable(
        route = "RackDetails/{rackId}",
        arguments = listOf(
            navArgument("rackId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val rackId = backStackEntry.arguments?.getString("rackId") ?: ""
        RackInformationScreen(
            rackId = rackId,
            navController = navController,
            modifier = Modifier
        )
    }

    // 添加货架
    composable("AddRack") {
        AddRackScreen(
            navController = navController,
            modifier = Modifier
        )
    }

    // 入库界面
    composable(
        route = "inStock/{orderId}/{sender}/{receiver}/{parcelCount}",
        arguments = listOf(
            navArgument("orderId") { type = NavType.StringType },
            navArgument("sender") { type = NavType.StringType },
            navArgument("receiver") { type = NavType.StringType },
            navArgument("parcelCount") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        val sender = backStackEntry.arguments?.getString("sender") ?: ""
        val receiver = backStackEntry.arguments?.getString("receiver") ?: ""
        val parcelCount = backStackEntry.arguments?.getInt("parcelCount") ?: 0

        InStockScreen(
            orderId = orderId,
            sender = sender,
            receiver = receiver,
            parcelCount = parcelCount,
            totalWeight = "0.0", // Default value since not available from QR scan
            navController = navController
        )
    }

    // 出库界面
    composable(
        route = "outStock/{orderId}/{sender}/{receiver}/{parcelCount}/{rackId}/{rackName}",
        arguments = listOf(
            navArgument("orderId") { type = NavType.StringType },
            navArgument("sender") { type = NavType.StringType },
            navArgument("receiver") { type = NavType.StringType },
            navArgument("parcelCount") { type = NavType.IntType },
            navArgument("rackId") { type = NavType.StringType },
            navArgument("rackName") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        val sender = backStackEntry.arguments?.getString("sender") ?: ""
        val receiver = backStackEntry.arguments?.getString("receiver") ?: ""
        val parcelCount = backStackEntry.arguments?.getInt("parcelCount") ?: 0
        val rackId = backStackEntry.arguments?.getString("rackId") ?: ""
        val rackName = backStackEntry.arguments?.getString("rackName") ?: ""

        OutStockScreen(
            orderId = orderId,
            sender = sender,
            receiver = receiver,
            parcelCount = parcelCount,
            rackId = rackId,
            currentRackName = rackName,
            navController = navController
        )
    }
}