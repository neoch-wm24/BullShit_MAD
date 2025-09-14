package com.example.user_management.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.user_management.ui.screens.SearchUserScreen
import com.example.user_management.ui.screens.CustomerDetailScreen
import com.example.user_management.ui.screens.AddUserScreen

@Composable
fun UserManagementNavHost(
    modifier: Modifier = Modifier,
) {
    val localNavController = rememberNavController()

    NavHost(
        navController = localNavController,
        startDestination = "search",
        modifier = modifier
    ) {
        // 搜索用户
        composable("search") {
            SearchUserScreen(
                navController = localNavController
            )
        }

        // 用户详情（带参数 userId）
        composable("customer_detail/{customerId}") { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId")
            customerId?.let {
                CustomerDetailScreen(
                    navController = localNavController,
                    customerId = it
                )
            }
        }

        // 添加用户
        composable("add_user") {
            AddUserScreen(
                navController = localNavController
            )
        }
    }
}
