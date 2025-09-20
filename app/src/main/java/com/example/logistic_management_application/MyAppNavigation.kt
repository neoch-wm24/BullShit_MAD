package com.example.logistic_management_application

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core_ui.components.BottomBar
import com.example.core_ui.components.TopBar
import com.example.core_ui.components.ScanScreen
import com.example.main_screen.ui.LoginPage
import com.example.main_screen.ui.SettingPage
import com.example.main_screen.ui.ProfilePage
import com.example.warehouse_management.WarehouseManagementNavHost
import com.example.warehouse_management.ui.screen.AddRackScreen
import com.example.warehouse_management.ui.screen.RackInformationScreen
import com.example.delivery_and_transportation_management.DeliveryAndTransportationNavHost
import com.example.main_screen.ui.HomePage
import com.example.main_screen.viewmodel.AuthState
import com.example.order_and_parcel_management.orderNavigation
import com.example.order_and_parcel_management.ui.screen.AddOrderScreen
import com.example.user_management.ui.UserManagementNavHost
import com.example.main_screen.viewmodel.AuthViewModel as MainScreenAuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: MainScreenAuthViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        topBar = {
            if (currentRoute != "login") {
                TopBar(navController = navController)
            }
        },
        bottomBar = {
            if (currentRoute != "login") {
                BottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = if (currentRoute == "login") Modifier.fillMaxSize() else Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginPage(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            composable("home") {
                val authState = authViewModel.authState.observeAsState()

                when (val state = authState.value) {
                    is AuthState.Authenticated -> {
                        HomePage(
                            navController = navController,
                            role = state.role
                        )
                    }
                    is AuthState.Loading -> {
                        androidx.compose.material3.Text("Loading...")
                    }
                    is AuthState.Error -> {
                        androidx.compose.material3.Text("Error: ${state.message}")
                    }
                    else -> {
                        androidx.compose.material3.Text("Not authenticated")
                    }
                }
            }
            composable("profile") {
                ProfilePage(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            composable("setting") {
                SettingPage(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
            composable("user") {
                UserManagementNavHost(
                    modifier = Modifier.fillMaxSize(),
                )
            }

            orderNavigation(navController)

            composable("warehouse") {
                WarehouseManagementNavHost(
                    mainNavController = navController,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable("delivery") {
                DeliveryAndTransportationNavHost(
                    modifier = Modifier.fillMaxSize(),
                )
            }
            composable("rack_information/{rackId}") { backStackEntry ->
                val rackId = backStackEntry.arguments?.getString("rackId") ?: ""
                RackInformationScreen(
                    navController = navController,
                    rackId = rackId,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("add_rack") {
                AddRackScreen(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("scan") {
                ScanScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}