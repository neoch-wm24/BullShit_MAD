package com.example.warehouse_management

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.warehouse_management.ui.screen.SearchRackScreen
import com.example.warehouse_management.ui.screen.AddRackScreen

@Composable
fun WarehouseManagementNavHost(
    mainNavController: NavHostController, // This is the main NavController
    modifier: Modifier = Modifier,
){
    val localNavController = rememberNavController()
    NavHost(
        navController = localNavController,
        startDestination = "search_rack",
        modifier = modifier
    ){
        composable("search_rack") {
            SearchRackScreen(
                navController = mainNavController, // Use main NavController for cross-module navigation
            )
        }

        composable("add_rack") {
            AddRackScreen(
                navController = localNavController, // Local NavController for internal navigation
                modifier = Modifier
            )
        }
    }
}