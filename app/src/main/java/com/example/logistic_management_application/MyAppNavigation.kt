package com.example.logistic_management_application

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core_ui.components.BottomBar
import com.example.core_ui.components.TopBar
import com.example.core_ui.components.ScanScreen
import com.example.main_screen.ui.LoginPage
import com.example.main_screen.ui.SettingPage
import com.example.main_screen.ui.ProfilePage
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.deliveryAndTransportationNavigation
import com.example.main_screen.ui.HomePage
import com.example.main_screen.viewmodel.AuthState
import com.example.order_management.orderNavigation
import com.example.user_management.userNavigation
import com.example.warehouse_management.warehouseNavigation
import com.example.main_screen.viewmodel.AuthViewModel as MainScreenAuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    deliveryViewModel: DeliveryViewModel = viewModel(),
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
                val authState by authViewModel.authState.observeAsState()
                if (authState is AuthState.Authenticated) {
                    val state = authState as AuthState.Authenticated
                    BottomBar(
                        navController = navController,
                        role = state.role,
                        employeeID = state.employeeID
                    )
                }
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

            // ✅ 参数化的 home route
            composable(
                route = "home/{role}/{employeeID}",
                arguments = listOf(
                    navArgument("role") { type = NavType.StringType },
                    navArgument("employeeID") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val role = backStackEntry.arguments?.getString("role") ?: "employee"
                val employeeID = backStackEntry.arguments?.getString("employeeID") ?: ""

                HomePage(
                    navController = navController,
                    role = role,
                    employeeID = employeeID
                )
            }

            // ✅ 新增无参的 'home' 别名，便于功能模块直接导航
            composable("home") {
                val authState by authViewModel.authState.observeAsState()
                val (role, employeeID) = when (val state = authState) {
                    is AuthState.Authenticated -> state.role to state.employeeID
                    else -> "employee" to ""
                }
                HomePage(
                    navController = navController,
                    role = role,
                    employeeID = employeeID
                )
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

            userNavigation(navController)

            orderNavigation(navController)

            warehouseNavigation(navController)

            deliveryAndTransportationNavigation(navController, deliveryViewModel)

            composable("scan") {
                ScanScreen(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
