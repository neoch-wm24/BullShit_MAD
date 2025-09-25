package com.example.warehouse_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.core_data.*
import java.util.Locale

@Composable
fun RackInformationScreen(
    rackId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    if (RackManager.rackList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No Rack data available.", color = Color.Gray, fontSize = 18.sp)
        }
        return
    }

    if (rackId.isBlank()) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val rackInfo = RackManager.getRackById(rackId)
    if (rackInfo == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Rack not found.", color = Color.Gray, fontSize = 18.sp)
        }
        return
    }

    // ✅ Use real-time data fetching from racks collection with refresh capability
    var orders by remember { mutableStateOf<List<AllParcelData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Function to refresh orders
    suspend fun refreshOrders() {
        try {
            isLoading = true
            errorMessage = null
            orders = ParcelDataManager.getOrdersFromRack(rackId)
            println("RackInformation: Loaded ${orders.size} orders for rack $rackId")
        } catch (e: Exception) {
            errorMessage = "Failed to load orders: ${e.message}"
            println("RackInformation: Error loading orders - ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // Load orders initially and set up auto-refresh
    LaunchedEffect(rackId) {
        refreshOrders()
    }

    // Add a refresh button or auto-refresh mechanism
    LaunchedEffect(Unit) {
        // Refresh every 30 seconds or when screen becomes visible
        kotlinx.coroutines.delay(1000) // Initial delay
        refreshOrders()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Rack Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF69B4),
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
            )
        }

        item { RackInfoCard(rackInfo = rackInfo) }

        if (orders.isNotEmpty()) {
            item {
                Text(
                    text = "Order List",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF69B4),
                    modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
                )
            }

            items(count = orders.size, key = { index -> orders[index].id }) { index ->
                val order = orders[index]
                OrderListCard(
                    orderData = order,
                    onClick = {
                        // ✅ REMOVED: No click action - display only
                        // Users can only outstock through QR code scanning
                    }
                )
            }
        } else {
            item {
                Text(
                    text = "No Orders available for this Rack",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(90.dp)) }
    }
}

@Composable
private fun RackInfoCard(rackInfo: RackInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            InfoRow("Rack Name:", rackInfo.name)
            DividerSpacer()
            InfoRow("Layers:", "${rackInfo.layer}")
            DividerSpacer()
            InfoRow("Rack State:", rackInfo.state)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun DividerSpacer() {
    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun OrderListCard(
    orderData: AllParcelData,
    onClick: () -> Unit = {} // Keep parameter for compatibility but make it optional
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
            // ✅ REMOVED: .clickable { onClick() } - No longer clickable
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Order ID: ${orderData.id}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Date: ${
                    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(orderData.timestamp)
                }",
                fontSize = 12.sp,
                color = Color.Gray
            )

            // ✅ Add visual indicator that this is display-only
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: ${orderData.status}",
                fontSize = 12.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium
            )
        }
    }
}