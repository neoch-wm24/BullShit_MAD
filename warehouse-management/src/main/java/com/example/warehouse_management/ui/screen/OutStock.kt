package com.example.warehouse_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.core_data.RackManager
import com.example.core_data.ParcelDataManager

@Composable
fun OutStockScreen(
    orderId: String,
    sender: String,
    receiver: String,
    parcelCount: Int,
    currentRackName: String,
    modifier: Modifier = Modifier,
    navController: NavHostController? = null
) {
    val currentRack = RackManager.getCurrentRack()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        // ---------- 扫描信息 Card ----------
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("出库单号: $orderId")
                Text("发件人: $sender")
                Text("收件人: $receiver")
                Text("包裹数量: $parcelCount")
            }
        }

        // ---------- Current Rack ----------
        Text(
            text = "Current Rack",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    if (currentRack.isNotEmpty()) currentRack else "No rack info available"
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---------- Outstock 按钮 ----------
        Button(
            onClick = {
                // 出库逻辑：清除 rack & 更新订单状态为 "Out-Stock"
                RackManager.clearCurrentRack()
                ParcelDataManager.updateOrderStatus(orderId, "Out-Stock")

                navController?.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentRack.isNotEmpty(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Outstock", color = Color.Black)
        }
    }
}