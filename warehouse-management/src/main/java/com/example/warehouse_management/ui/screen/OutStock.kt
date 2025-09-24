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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    // 🔑 通过 orderId 找订单
    val order = ParcelDataManager.getOrderById(orderId)
    val rackInfo = order?.rackId?.let { rackId ->
        RackManager.getRackById(rackId)
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
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
                    rackInfo?.name ?: "No rack info available"
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    // ✅ 更新订单状态即可，不用依赖 RackManager.currentRack
                    ParcelDataManager.updateOrderStatus(orderId, "Out-Stock")
                    navController?.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = rackInfo != null, // ✅ 只有有 rackInfo 时才允许出库
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Outstock", color = Color.Black)
        }
    }
}