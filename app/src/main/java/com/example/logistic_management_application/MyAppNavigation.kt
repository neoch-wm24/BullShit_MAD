package com.example.logistic_management_application

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.core_ui.components.BottomNavBar
import com.example.core_ui.components.PageTitleBar
import com.example.logistic_management_application.ui.mainscreen.pages.LoginPage
import com.example.logistic_management_application.ui.mainscreen.pages.SettingPage
import com.example.order_and_parcel_management.ui.OrderandParcelManagementNavHost
import com.example.warehouse_management.ui.WarehouseManagementNavHost
import com.example.warehouse_management.ui.screen.AddRakScreen
import com.example.warehouse_management.ui.screen.RakInformationScreen
import com.example.user_management.ui.*
import screen.DeliveryAndTransportationNavHost
import com.example.main_screen.ui.HomePage
import com.example.main_screen.viewmodel.AuthViewModel as MainScreenAuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: com.example.logistic_management_application.ui.modules.user.AuthViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier,
        topBar = {
            PageTitleBar(navController = navController)
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginPage(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            composable("home") {
                val mainScreenAuthViewModel: MainScreenAuthViewModel = viewModel()
                HomePage(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    authViewModel = mainScreenAuthViewModel
                )
            }
            composable("setting") {
                SettingPage(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            composable("profile") {
                // Temporary placeholder for ProfilePage until it's available
                androidx.compose.material3.Text(
                    text = "Profile Page - Coming Soon",
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("order") {
                OrderandParcelManagementNavHost(
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable("user") {
                UserScreen(modifier, navController)
            }

            composable("delivery") {
                DeliveryAndTransportationNavHost(
                    modifier = Modifier.fillMaxSize(),
                )
            }

            composable("warehouse") {
                WarehouseManagementNavHost(
                    mainNavController = navController,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            composable("rak_information/{rakId}") { backStackEntry ->
                val rakId = backStackEntry.arguments?.getString("rakId") ?: ""
                RakInformationScreen(
                    navController = navController,
                    rakId = rakId,
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable("add_rak") {
                AddRakScreen(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable("scan") {
                // Temporary placeholder for ScanScreen until it's available
                androidx.compose.material3.Text(
                    text = "Scan Screen - Coming Soon",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}