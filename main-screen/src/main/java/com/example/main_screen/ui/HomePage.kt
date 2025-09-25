package com.example.main_screen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.core_ui.components.Dashboard
import com.example.core_ui.components.DashboardItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomePage(navController: NavController, role: String, employeeID: String? = null) {
    when (role) {
        "admin" -> {
            AdminHomeScreen(navController)
        }
        "driver" -> {
            if (employeeID != null) {
                com.example.delivery_and_transportation_management.ui.screen.DriverHome(
                    navController = navController,
                    employeeID = employeeID
                )
            } else {
                EmployeeHomeScreen(navController)
            }
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

        QuickReportCard(navController = navController, role = "admin")
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

        QuickReportCard(navController = navController, role = "employee")
    }
}

@Composable
fun QuickReportCard(navController: NavController, role: String) {
    val db = FirebaseFirestore.getInstance()
    val todayStr = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date())

    var ordersToday by remember { mutableStateOf(0) }
    var pendingInStock by remember { mutableStateOf(0) }
    var parcelsToday by remember { mutableStateOf(0) }
    var totalParcels by remember { mutableStateOf(0) }
    var totalRacks by remember { mutableStateOf(0) }
    var idleRacks by remember { mutableStateOf(0) }
    var nonIdleRacks by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        // Orders
        val ordersSnap = db.collection("orders").get().await()
        val orders = ordersSnap.documents
        ordersToday = orders.count {
            it.id.contains(todayStr) || (it.getString("id")?.contains(todayStr) == true)
        }
        pendingInStock = orders.count { !it.contains("status") }

        // Parcels
        val parcelsSnap = db.collection("parcels").get().await()
        val parcels = parcelsSnap.documents
        totalParcels = parcels.size
        parcelsToday = parcels.count {
            it.id.contains(todayStr) || (it.getString("id")?.contains(todayStr) == true)
        }

        // Racks
        val racksSnap = db.collection("racks").get().await()
        val racks = racksSnap.documents
        totalRacks = racks.size
        idleRacks = racks.count { it.getString("state") == "Idle" }
        nonIdleRacks = racks.count { it.getString("state") == "Non-Idle" }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Quick Report",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (role == "admin") {
                TextButton(onClick = { navController.navigate("report") }) {
                    Text("More >>", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 300.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionTitle("Orders Overview")
                ReportRow("Today's Orders", ordersToday.toString())
                ReportRow("Pending In-Stock", pendingInStock.toString())

                Divider()

                SectionTitle("Parcels Overview")
                ReportRow("Today's Parcels", parcelsToday.toString())
                ReportRow("Total Parcels", totalParcels.toString())

                Divider()

                SectionTitle("Warehouse Overview")
                ReportRow("Total Racks", totalRacks.toString())
                ReportRow("Idle Racks", idleRacks.toString())
                ReportRow("Non-Idle Racks", nonIdleRacks.toString())
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun ReportRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}


