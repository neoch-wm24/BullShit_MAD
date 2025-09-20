package com.example.order_management.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.core_ui.components.BottomBar
import com.example.core_ui.components.TopBar
import com.example.core_ui.components.SearchBar
import com.example.core_ui.components.FilterBy
import com.example.core_ui.theme.LogisticManagementApplicationTheme
import com.example.order_management.ui.components.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

// 数据类
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
    var selectedFilter by remember { mutableStateOf("name (A~Z)") }

    // ✅ Firestore 实时数据
    val db = FirebaseFirestore.getInstance()
    var orders by remember { mutableStateOf<List<OrderSummary>>(emptyList()) }

    // 🔄 使用 DisposableEffect 来管理监听
    DisposableEffect(Unit) {
        val listener = db.collection("orders")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("监听 Firestore 出错: ${e.message}")
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

                    tempOrders.forEach { (ids, parcels) ->
                        val (id, senderId, receiverId) = ids

                        // 查 sender
                        db.collection("customers")
                            .whereEqualTo("id", senderId)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { senderSnap ->
                                val senderName = senderSnap.documents.firstOrNull()?.getString("name") ?: "未知"

                                // 查 receiver
                                db.collection("customers")
                                    .whereEqualTo("id", receiverId)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener { receiverSnap ->
                                        val receiverName = receiverSnap.documents.firstOrNull()?.getString("name") ?: "未知"

                                        // 更新 orders
                                        orders = orders.toMutableList().apply {
                                            removeAll { it.id == id }
                                            add(
                                                OrderSummary(
                                                    id = id,
                                                    senderName = senderName,
                                                    receiverName = receiverName,
                                                    parcelCount = parcels
                                                )
                                            )
                                        }
                                    }
                            }
                    }
                }
            }

        onDispose { listener.remove() }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                navController = navController,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // ✅ 使用 contentPadding
                .padding(16.dp)
                .background(Color.White)
        ) {
            // 搜索框
            SearchBar(
                value = searchText,
                onValueChange = { searchText = it },
                label = "Search Order",
                placeholder = "Order ID",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 筛选器
            FilterBy(
                selectedFilter = selectedFilter,
                options = listOf("name (A~Z)", "name (Z~A)", "Idle Rack", "Non-Idle Rack"),
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
                        text = "暂无包裹，请点击右下角 + 按钮添加",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(8.dp) // ✅ 给列表加 padding
                ) {
                    items(
                        orders.filter {
                            searchText.isEmpty() || it.id.contains(searchText, ignoreCase = true)
                        }
                    ) { order ->
                        OrderListItem(order = order, onClick = {
                            navController.navigate("OrderDetails/${order.id}")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun OrderListItem(order: OrderSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("订单号: ${order.id}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("寄件者: ${order.senderName}")
            Text("收件者: ${order.receiverName}")
            Text("包裹数量: ${order.parcelCount}")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OrderAndParcelWithNavigationPreview() {
    LogisticManagementApplicationTheme {
        val navController = rememberNavController()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8)),
            topBar = {
                TopBar(navController = navController)
            },
            bottomBar = {
                BottomBar(navController = navController)
            }
        ) { innerPadding ->
            SearchOrderAndParcelScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                navController = navController,
            )
        }
    }
}