package com.example.warehouse_management

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.warehouse_management.ui.screen.SearchRakScreen
import com.example.warehouse_management.ui.screen.AddRakScreen

@Composable
fun WarehouseManagementNavHost(
    mainNavController: NavHostController, // This is the main NavController
    modifier: Modifier = Modifier,
){
    val localNavController = rememberNavController()
    NavHost(
        navController = localNavController,
        startDestination = "searchrak",
        modifier = modifier
    ){
        composable("searchrak") {
            SearchRakScreen(
                navController = mainNavController, // Use main NavController for cross-module navigation
            )
        }

        composable("addrak") {
            AddRakScreen(
                navController = localNavController, // Local NavController for internal navigation
                modifier = Modifier
            )
        }
    }
}