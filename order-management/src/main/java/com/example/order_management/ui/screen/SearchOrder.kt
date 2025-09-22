package com.example.order_management.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.core_ui.components.SearchBar
import com.example.core_ui.components.FilterBy
import com.example.order_management.ui.components.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Data class
data class OrderSummary(
    val id: String,
    val senderName: String,
    val receiverName: String,
    val parcelCount: Int
)

@Composable
fun SearchOrderAndParcelScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Order ID (A~Z)") }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedOrders by remember { mutableStateOf(setOf<OrderSummary>()) }
    val db = FirebaseFirestore.getInstance()
    var orders by remember { mutableStateOf<List<OrderSummary>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // 管理删除状态
    var isDeleting by remember { mutableStateOf(false) }

    // 异步获取客户名称的辅助函数
    suspend fun getCustomerName(db: FirebaseFirestore, customerId: String): String {
        return try {
            val querySnapshot = db.collection("customers")
                .whereEqualTo("id", customerId)
                .limit(1)
                .get()
                .await()

            querySnapshot.documents.firstOrNull()?.getString("name") ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    DisposableEffect(Unit) {
        val listener = db.collection("orders")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Firestore listener error: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tempOrders = snapshot.documents.mapNotNull { doc ->
                        val id = doc.getString("id") ?: return@mapNotNull null
                        val senderId = doc.getString("sender_id") ?: ""
                        val receiverId = doc.getString("receiver_id") ?: ""
                        val parcels = (doc.get("parcel_ids") as? List<*>)?.size ?: 0
                        Triple(id, senderId, receiverId) to parcels
                    }

                    // 使用协程来处理异步查询，避免嵌套回调
                    coroutineScope.launch {
                        val newOrders = mutableListOf<OrderSummary>()

                        tempOrders.forEach { (ids, parcels) ->
                            val (id, senderId, receiverId) = ids

                            try {
                                // 查询发送者
                                val senderName = getCustomerName(db, senderId)
                                // 查询接收者
                                val receiverName = getCustomerName(db, receiverId)

                                newOrders.add(
                                    OrderSummary(
                                        id = id,
                                        senderName = senderName,
                                        receiverName = receiverName,
                                        parcelCount = parcels
                                    )
                                )
                            } catch (exception: Exception) {
                                println("Error fetching customer data for order $id: ${exception.message}")
                                // 即使出错也添加订单，使用默认名称
                                newOrders.add(
                                    OrderSummary(
                                        id = id,
                                        senderName = "Unknown",
                                        receiverName = "Unknown",
                                        parcelCount = parcels
                                    )
                                )
                            }
                        }

                        // 原子更新orders状态
                        orders = newOrders
                    }
                }
            }

        onDispose { listener.remove() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp, 16.dp, 16.dp, bottom = 25.dp)
        ) {
            // Search bar
            SearchBar(
                value = searchText,
                onValueChange = { searchText = it },
                label = "Search Order",
                placeholder = "Order ID",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter
            FilterBy(
                selectedFilter = selectedFilter,
                options = listOf("Order ID (A~Z)", "Order ID (Z~A)"),
                onFilterChange = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No orders found, please click the + button at the bottom right to add.",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                val filteredOrders = orders
                    .filter {
                        searchText.isEmpty() || it.id.contains(searchText, ignoreCase = true)
                    }
                    .let { list ->
                        when (selectedFilter) {
                            "Order ID (A~Z)" -> list.sortedBy { it.id }
                            "Order ID (Z~A)" -> list.sortedByDescending { it.id }
                            else -> list
                        }
                    }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(8.dp, bottom = if (isMultiSelectMode) 60.dp else 8.dp),
                ) {
                    items(filteredOrders) { order ->
                        // 检查订单是否正在删除中或已被删除
                        val isOrderDeleting = isDeleting && selectedOrders.any { it.id == order.id }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isMultiSelectMode) {
                                Checkbox(
                                    checked = order in selectedOrders && !isOrderDeleting,
                                    onCheckedChange = { checked ->
                                        if (!isOrderDeleting) {
                                            selectedOrders = if (checked) {
                                                selectedOrders + order
                                            } else {
                                                selectedOrders - order
                                            }
                                        }
                                    },
                                    enabled = !isOrderDeleting
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            OrderListItem(
                                order = order,
                                isDeleting = isOrderDeleting,
                                onClick = {
                                    if (!isOrderDeleting) {
                                        if (isMultiSelectMode) {
                                            selectedOrders = if (order in selectedOrders) {
                                                selectedOrders - order
                                            } else {
                                                selectedOrders + order
                                            }
                                        } else {
                                            navController.navigate("OrderDetails/${order.id}")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (!isMultiSelectMode) {
            FloatingActionButton(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onToggleMultiSelect = {
                    isMultiSelectMode = true
                    selectedOrders = emptySet()
                }
            )
        }

        if (isMultiSelectMode) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 8.dp, end = 8.dp, bottom = 25.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDeleting) {
                            "Deleting... ${selectedOrders.size}"
                        } else {
                            "Selected: ${selectedOrders.size}"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Row {
                        TextButton(
                            onClick = {
                                selectedOrders = emptySet()
                                isMultiSelectMode = false
                                isDeleting = false
                            },
                            enabled = !isDeleting
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        // 修复后的删除逻辑部分
                        IconButton(
                            onClick = {
                                if (selectedOrders.isNotEmpty() && !isDeleting) {
                                    coroutineScope.launch {
                                        // 设置删除状态
                                        isDeleting = true

                                        try {
                                            // 批量删除选中的订单
                                            val ordersToDelete = selectedOrders.toList()
                                            val deleteIds = ordersToDelete.map { it.id }

                                            // 批量删除Firestore文档
                                            val batch = db.batch()
                                            deleteIds.forEach { orderId ->
                                                val orderDoc = db.collection("orders")
                                                    .document(orderId)
                                                batch.delete(orderDoc)
                                            }

                                            // 执行批量删除
                                            batch.commit().await()

                                            // 同时调用仓库方法删除相关数据（如果需要）
                                            ordersToDelete.forEach { order ->
                                                try {
                                                    OrderRepository.deleteOrderWithParcels(order.id)
                                                } catch (e: Exception) {
                                                    println("Error deleting order ${order.id} from repository: ${e.message}")
                                                }
                                            }

                                        } catch (e: Exception) {
                                            println("Error during batch delete: ${e.message}")
                                        } finally {
                                            // 重置状态
                                            isDeleting = false
                                            selectedOrders = emptySet()
                                            isMultiSelectMode = false
                                        }
                                    }
                                }
                            },
                            enabled = selectedOrders.isNotEmpty() && !isDeleting
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = if (isDeleting) Color.Gray else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderListItem(
    order: OrderSummary,
    isDeleting: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDeleting) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDeleting) {
                Color(0xFFE0E0E0) // 变灰表示正在删除
            } else {
                Color(0xFFF8F8F8)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDeleting) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Order ID: ${order.id}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDeleting) Color.Gray else MaterialTheme.colorScheme.onSurface
                )

                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Sender: ${order.senderName}",
                color = if (isDeleting) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Receiver: ${order.receiverName}",
                color = if (isDeleting) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Parcels: ${order.parcelCount}",
                color = if (isDeleting) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}