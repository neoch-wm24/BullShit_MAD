package com.example.main_screen.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.core_ui.components.Dashboard
import com.example.core_ui.components.DashboardItem
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomePage(navController: NavController, role: String, employeeID: String? = null) {
    when (role) {
        "admin" -> {
            AdminHomeScreen(navController)
        }
        "driver" -> {

        }
        else -> {
            EmployeeHomeScreen(navController)
        }
    }
}



@Composable
private fun AdminHomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Dashboard(
            navController = navController,
            items = listOf(
                DashboardItem("User", Icons.Default.Person, "user"),
                DashboardItem("Order", Icons.Default.ShoppingCart, "order"),
                DashboardItem("Warehouse", Icons.Default.Home, "warehouse"),
                DashboardItem("Delivery", Icons.AutoMirrored.Filled.Send, "delivery"),
            )
        )
    }
}

@Composable
private fun EmployeeHomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Dashboard(
            navController = navController,
            items = listOf(
                DashboardItem("Order", Icons.Default.ShoppingCart, "order"),
                DashboardItem("Warehouse", Icons.Default.Home, "warehouse"),
            )
        )
    }
}
