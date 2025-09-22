package com.example.delivery_and_transportation_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.data.OrderWithCustomerNames

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransportToOrderScreen(
    selectedDate: String,
    selectedTransportIds: Set<String>,
    deliveries: List<Delivery>,
    ordersWithNames: List<OrderWithCustomerNames>,
    navController: NavController,
    deliveryViewModel: DeliveryViewModel,
    onAssignOrders: (Set<String>) -> Unit // 👈 保留回调参数
) {
    var selectedOrders by rememberSaveable { mutableStateOf(setOf<String>()) }

    // 已选的运输工具
    val selectedTransports = deliveries.filter { it.id in selectedTransportIds }

    // 计算所有已分配的订单
    val allAssignedOrders = remember(deliveries) {
        deliveries.flatMap { it.assignedOrders }.toSet()
    }

    // 过滤未分配的订单
    val filteredOrdersWithNames = ordersWithNames.filter {
        !allAssignedOrders.contains(it.order.id)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 日期 + 运输工具信息
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivery Schedule", style = MaterialTheme.typography.titleMedium)
                    Text("Date: $selectedDate", style = MaterialTheme.typography.bodyMedium)
                    Text("Transports: ${selectedTransports.size} selected",
                        style = MaterialTheme.typography.bodyMedium)
                    selectedTransports.forEach { transport ->
                        val plate = transport.plateNumber?.takeIf { it.isNotBlank() } ?: "(No Plate)"
                        Text("• $plate - ${transport.type} (${transport.driverName})",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                    }
                }
            }
        }

        // 订单列表标题
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Orders to Assign", style = MaterialTheme.typography.titleMedium)
                if (selectedOrders.isNotEmpty()) {
                    Text("Selected: ${selectedOrders.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // 列出 filtered orders with names
        if (filteredOrdersWithNames.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No unassigned orders available.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(filteredOrdersWithNames) { orderWithNames ->
                val order = orderWithNames.order
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedOrders.contains(order.id))
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Checkbox(
                            checked = selectedOrders.contains(order.id),
                            onCheckedChange = { checked ->
                                selectedOrders = if (checked) selectedOrders + order.id else selectedOrders - order.id
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Order #${order.id.take(12)}",
                                style = MaterialTheme.typography.titleSmall)

                            // 🔥 显示客户名称而不是 ID
                            Text("Sender: ${orderWithNames.senderName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary)
                            Text("Receiver: ${orderWithNames.receiverName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary)

                            // 显示包裹信息
                            if (order.parcelIds.isNotEmpty()) {
                                Text("Parcels: ${order.parcelIds.size} item(s)",
                                    style = MaterialTheme.typography.bodySmall)
                                // 可以选择性地显示前几个包裹ID
                                val displayParcels = if (order.parcelIds.size > 3) {
                                    order.parcelIds.take(3).joinToString(", ") + "..."
                                } else {
                                    order.parcelIds.joinToString(", ")
                                }
                                Text("IDs: $displayParcels",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // 订单摘要信息
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Weight: ${order.totalWeight} kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Cost: RM ${"%.2f".format(order.cost)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // 按钮
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (selectedOrders.isNotEmpty()) {
                            // 👈 使用回调而不是直接调用 ViewModel 方法
                            onAssignOrders(selectedOrders)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedOrders.isNotEmpty()
                ) {
                    val buttonText = if (selectedOrders.isEmpty()) {
                        "Select orders first"
                    } else {
                        "Assign ${selectedOrders.size} Order(s) to Transport"
                    }
                    Text(buttonText)
                }

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}