package com.example.main_screen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.renderer.SimpleSliceDrawer
import com.example.core_ui.theme.Green
import com.example.core_ui.theme.Red
import com.example.core_ui.theme.Black
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color

@Composable
fun EmployeeHome(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Dashboard(
            navController = navController,
            items = listOf(
                DashboardItem("Orders", Icons.Default.ShoppingCart, "order"),
                DashboardItem("Warehouse", Icons.Default.Home, "warehouse"),
            )
        )

        OverviewCard()
        RackStateCard()
    }
}

@Composable
fun OverviewCard() {
    val allOrders = ParcelDataManager.allParcelData
    val totalOrders = allOrders.size
    val inStockOrders = allOrders.count { it.status == "In-Stock" }
    val outStockOrders = allOrders.count { it.status == "Out-Stock" }

    // Simulate 7-day trend data
    val inStockTrend = (1..7).map { day ->
        LineChartData.Point(
            value = (inStockOrders / 7f) + (day % 3) * 2f, // Simulate fluctuation
            label = "Day $day"
        )
    }
    val outStockTrend = (1..7).map { day ->
        LineChartData.Point(
            value = (outStockOrders / 7f) + ((day + 1) % 4) * 1.5f,
            label = "Day $day"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📈 Overview", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Black)

            // Double line chart - pass ARGB Int (toArgb) to ensure library renders correct colors
            LineChart(
                linesChartData = listOf(
                    LineChartData(
                        points = inStockTrend,
                        lineDrawer = SolidLineDrawer(color = Green)
                    ),
                    LineChartData(
                        points = outStockTrend,
                        lineDrawer = SolidLineDrawer(color = Red)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            // Bottom statistics (text still uses Compose Color)
            Text("Total Orders: $totalOrders", fontSize = 14.sp, color = Black)
            Text("In-Stock Orders: $inStockOrders", fontSize = 14.sp, color = Green)
            Text("Out-Stock Orders: $outStockOrders", fontSize = 14.sp, color = Red)
        }
    }
}

@Composable
fun RackStateCard() {
    val totalRacks = RackManager.rackList.size
    val idleCount = RackManager.rackList.count { it.state == "Idle" }
    val nonIdleCount = totalRacks - idleCount
    val idleRate = if (totalRacks > 0) (idleCount * 100) / totalRacks else 0
    val nonIdleRate = 100 - idleRate

    // Debug: Print values to console
    println("Debug - Total: $totalRacks, Idle: $idleCount, NonIdle: $nonIdleCount")

    // Try multiple approaches for pie chart colors
    val greenColor = Color(0xFF4CAF50)
    val redColor = Color(0xFFF44336)

    // Ensure we have some data to display
    val pieChartData = PieChartData(
        slices = if (totalRacks > 0) {
            listOf(
                PieChartData.Slice(
                    value = if (idleCount > 0) idleCount.toFloat() else 1f,
                    color = greenColor
                ),
                PieChartData.Slice(
                    value = if (nonIdleCount > 0) nonIdleCount.toFloat() else 1f,
                    color = redColor
                )
            )
        } else {
            // Default data if no racks
            listOf(
                PieChartData.Slice(value = 1f, color = Color.Gray)
            )
        }
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🎯 Rack Status", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Black)

            if (totalRacks > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Total Racks: $totalRacks", fontSize = 14.sp, color = Black)
                        Text("Idle Rate: $idleRate%", fontSize = 14.sp, color = Green)
                        Text("Non-Idle Rate: $nonIdleRate%", fontSize = 14.sp, color = Red)
                    }

                    PieChart(
                        pieChartData = pieChartData,
                        sliceDrawer = SimpleSliceDrawer(),
                        modifier = Modifier.size(100.dp)
                    )
                }
            } else {
                Text("No rack data available", fontSize = 14.sp, color = Black)
            }
        }
    }
}