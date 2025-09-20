package com.example.main_screen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.core_ui.components.Dashboard
import com.example.core_ui.components.DashboardItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart

import com.example.core_data.ParcelDataManager
import com.example.core_data.RackManager
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color

@Composable
fun EmployeeHome(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 上方按钮区
        Dashboard(
            navController = navController,
            items = listOf(
                DashboardItem("Order", Icons.Default.ShoppingCart, "order"),
                DashboardItem("Warehouse", Icons.Default.Home, "warehouse"),
            )
        )

        // 📊 可视化区
        Text("Rack 状态分布", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        RackStateChart()

        Text("Rack 层数分布", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        RackLayerChart()

        Text("订单趋势", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        OrderTrendChart()
    }
}

@Composable
fun RackStateChart() {
    val rackList = RackManager.rackList
    val idleCount = rackList.count { it.state == "Idle" }
    val nonIdleCount = rackList.size - idleCount
    val totalRacks = rackList.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Total Racks: $totalRacks", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Idle: $idleCount", color = MaterialTheme.colorScheme.primary)
                Text("Active: $nonIdleCount", color = MaterialTheme.colorScheme.secondary)
            }
            if (totalRacks > 0) {
                val idlePercentage = (idleCount * 100) / totalRacks
                Text("Idle Rate: $idlePercentage%", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun RackLayerChart() {
    val rackList = RackManager.rackList
    val grouped = rackList.groupBy { it.layer }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Rack Distribution by Layer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            grouped.toSortedMap().forEach { (layer, racks) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Layer $layer:", fontSize = 14.sp)
                    Text("${racks.size} racks", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun OrderTrendChart() {
    val orders = ParcelDataManager.allParcelData
    val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
    val grouped = orders.groupBy { dateFormat.format(it.timestamp) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Order Statistics", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Total Orders: ${orders.size}", fontSize = 14.sp)
            if (grouped.isNotEmpty()) {
                Text("Recent Activity:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                grouped.toList().takeLast(3).forEach { (date, dayOrders) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(date, fontSize = 12.sp)
                        Text("${dayOrders.size} orders", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}