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
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.core_ui.components.Dashboard
import com.example.core_ui.components.DashboardItem

@Composable
fun HomePage(navController: NavController, role: String, employeeID: String? = null) {
    when (role) {
        "admin" -> AdminHome(navController)
        "driver" -> DriverHome(navController, employeeID)
        else -> EmployeeHome(navController)
    }
}

@Composable
fun AdminHome(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // 保证不会顶到TopBar
        verticalArrangement = Arrangement.spacedBy(24.dp) // 组件之间有间距
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
fun DriverHome(navController: NavController, employeeID: String?) {
    com.example.delivery_and_transportation_management.ui.screen.DriverHome(
        navController = navController,
        employeeID = employeeID ?: "",
        // deliveryViewModel will be provided by the delivery module's NavHost or via viewModel()
    )
}
