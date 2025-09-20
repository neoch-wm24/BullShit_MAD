package com.example.warehouse_management

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.warehouse_management.ui.screen.SearchRakScreen
import com.example.warehouse_management.ui.screen.AddRakScreen
import com.example.warehouse_management.ui.screen.RakInformationScreen

fun NavGraphBuilder.warehouseNavigation(navController: NavHostController) {
    // Order 相关页面
    composable("warehouse") {
        SearchRakScreen(
            navController = navController
        )
    }

    composable("RakDetails/{rakId}"
    ) { backStackEntry ->
        val rakId = backStackEntry.arguments?.getString("rakId") ?: ""
        RakInformationScreen(
            rakId = rakId,
            modifier = Modifier
        )
    }

    composable("AddRak") {
        AddRakScreen(
            navController = navController,
            modifier = Modifier
        )
    }
}