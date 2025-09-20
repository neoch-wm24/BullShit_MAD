package com.example.user_management

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.user_management.ui.screens.SearchUserScreen
import com.example.user_management.ui.screens.CustomerDetailScreen
import com.example.user_management.ui.screens.AddUserScreen
import com.example.user_management.ui.screens.EditUserScreen

fun NavGraphBuilder.userNavigation(navController: NavHostController) {
    // Order 相关页面
    composable("user") {
        SearchUserScreen(
            navController = navController
        )
    }

    // 用户详情（带参数 userId）
    composable("CustomerDetails/{customerId}") { backStackEntry ->
        val customerId = backStackEntry.arguments?.getString("customerId")
        customerId?.let {
            CustomerDetailScreen(
                navController = navController,
                customerId = it
            )
        }
    }

    composable("Edit_User/{customerId}") { backStackEntry ->
        val customerId = backStackEntry.arguments?.getString("customerId")
        customerId?.let {
            EditUserScreen(
                navController = navController,
                customerId = it
            )
        }
    }

    // 添加用户
    composable("AddUser") {
        AddUserScreen(
            navController = navController
        )
    }
}