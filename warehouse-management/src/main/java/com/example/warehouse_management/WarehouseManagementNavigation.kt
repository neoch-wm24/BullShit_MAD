package com.example.warehouse_management

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.warehouse_management.ui.screen.SearchRackScreen
import com.example.warehouse_management.ui.screen.AddRackScreen
import com.example.warehouse_management.ui.screen.RackInformationScreen

fun NavGraphBuilder.warehouseNavigation(navController: NavHostController) {
    // Order 相关页面
    composable("warehouse") {
        SearchRackScreen(
            navController = navController
        )
    }

    composable("RackDetails/{rackId}"
    ) { backStackEntry ->
        val rackId = backStackEntry.arguments?.getString("rackId") ?: ""
        RackInformationScreen(
            rackId = rackId,
            navController = navController,
            modifier = Modifier
        )
    }

    composable("AddRack") {
        AddRackScreen(
            navController = navController,
            modifier = Modifier
        )
    }
}