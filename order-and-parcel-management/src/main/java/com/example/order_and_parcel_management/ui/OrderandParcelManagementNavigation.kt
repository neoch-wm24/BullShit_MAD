package com.example.order_and_parcel_management.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.order_and_parcel_management.ui.screen.SearchOrderAndParcelScreen
import com.example.order_and_parcel_management.ui.screen.SelectOrderandParcelScreen
import com.example.order_and_parcel_management.ui.screen.AddOrderandParcelScreen

@Composable
fun OrderandParcelManagementNavHost(
    modifier: Modifier = Modifier,
) {
    val localNavController = rememberNavController()

    NavHost(
        navController = localNavController,
        startDestination = "search",


        modifier = modifier
    ) {
        composable("search") {
            SearchOrderAndParcelScreen(
                navController = localNavController,
            )
        }
        composable("multiple_select") {
            SelectOrderandParcelScreen(
                navController = localNavController)
        }
        composable("add") {
            AddOrderandParcelScreen(
                navController = localNavController)
        }
    }
}