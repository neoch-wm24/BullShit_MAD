package com.example.main_screen.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class DashboardItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun HomePage(navController: NavController, role: String) {
    when (role) {
        "admin" -> AdminHome(navController)
        "driver" -> DriverHome(navController)
        else -> EmployeeHome(navController)
    }
}

@Composable
fun AdminHome(navController: NavController) {
    Dashboard(
        navController = navController,
        items = listOf(
            DashboardItem("User", Icons.Default.Person, "user"),
            DashboardItem("Order", Icons.Default.ShoppingCart, "order"),
            DashboardItem("Warehouse", Icons.Default.Home, "warehouse"),
            DashboardItem("Delivery", Icons.AutoMirrored.Filled.Send, "delivery"),
        )
    )

    Text(text = "This is Admin Home Page")
}

@Composable
fun DriverHome(navController: NavController) {
    Dashboard(
        navController = navController,
        items = listOf(
            DashboardItem("Order", Icons.Default.ShoppingCart, "order"),
            DashboardItem("Delivery", Icons.AutoMirrored.Filled.Send, "delivery"),
        )
    )
    Text(text = "This is Driver Home Page")
}

@Composable
fun EmployeeHome(navController: NavController) {
    Dashboard(
        navController = navController,
        items = listOf(
            DashboardItem("User", Icons.Default.Person, "user"),
            DashboardItem("Order", Icons.Default.ShoppingCart, "order"),
            DashboardItem("Warehouse", Icons.Default.Home, "warehouse"),
            DashboardItem("Delivery", Icons.AutoMirrored.Filled.Send, "delivery"),
        )
    )
    Text(text = "This is Employee Home Page")
}

@Composable
fun Dashboard(
    navController: NavController,
    items: List<DashboardItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Dashboard", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items.chunked(2).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach {
                        DashboardButton(item = it, navController, modifier = Modifier.weight(1f))
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun DashboardButton(item: DashboardItem, navController: NavController, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { navController.navigate(item.route) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,        // 白色背景
            contentColor = Color.Black           // 内容默认黑色
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFFFF69B4) // 图标热粉
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black // 文字黑色
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    val navController = rememberNavController()
    AdminHome(navController)
}
