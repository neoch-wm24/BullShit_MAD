package com.example.warehouse_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.core_data.*
import java.util.Locale

@Composable
fun RackInformationScreen(
    rackId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // ① 判断是否有 Rack 数据
    if (RackManager.rackList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No Rack data available.", color = Color.Gray, fontSize = 18.sp)
        }
        return
    }

    // ② 空 rackId 直接返回上页
    if (rackId.isBlank()) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    // ③ 查找 Rack 数据
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

    // ④ 获取该 Rack 下的订单（只取 In-Stock，且 rackId 匹配）
    val orders: List<AllParcelData> = ParcelDataManager
        .getInStockOrders()
        .filter { it.rackId == rackId }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Rack Information Title
        item {
            Text(
                text = "Rack Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF69B4),
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
            )
        }

        // Rack Info
        item {
            RackInfoCard(rackInfo = rackInfo)
        }

        // Order List
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
                OrderListCard(orderData = order)
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

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
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
fun OrderListCard(orderData: AllParcelData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Order ID
            Text(
                text = "Order ID: ${orderData.id}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Date
            Text(
                text = "Date: ${
                    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(orderData.timestamp)
                }",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RackInformationScreenPreview() {
    val navController = rememberNavController()
    RackInformationScreen(rackId = "SampleRackId", navController = navController)
}