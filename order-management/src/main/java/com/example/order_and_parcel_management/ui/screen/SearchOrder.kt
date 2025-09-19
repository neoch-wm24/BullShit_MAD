package com.example.order_and_parcel_management.ui.screen

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.core_ui.components.BottomBar
import com.example.core_ui.components.TopBar
import com.example.core_ui.components.SearchBar
import com.example.core_ui.theme.LogisticManagementApplicationTheme
import com.example.order_and_parcel_management.ui.components.FloatingActionButton
import com.example.core_data.ParcelDataManager

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
    var isSearchActive by remember { mutableStateOf(false) }

    // Get real data from ParcelDataManager with proper reactivity
    val allOrders by remember {
        derivedStateOf { ParcelDataManager.allParcelData }
    }

    val orders = remember(allOrders) {
        allOrders.map { orderData ->
            OrderSummary(
                id = orderData.id,
                senderName = orderData.sender.name,
                receiverName = orderData.recipient.name,
                parcelCount = orderData.parcels.size
            )
        }
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
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // Search Bar
            SearchBar(
                searchText = searchText,
                onSearchTextChange = {
                    searchText = it
                    isSearchActive = it.isNotEmpty()
                },
                onClearSearch = {
                    searchText = ""
                    isSearchActive = false
                },
                isSearchActive = isSearchActive
            )

            // Filter By dropdown
            FilterBy(
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it }
            )

            if (orders.isEmpty()) {
                // 没有订单时显示提示
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
                // LazyColumn 显示订单列表
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        orders.filter {
                            searchText.isEmpty() || it.id.contains(searchText, ignoreCase = true)
                        }
                    ) { order ->
                        OrderListItem(order = order, onClick = {
                            navController.navigate("order_detail/${order.id}")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBy(
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    val options = listOf(
        "name (A~Z)", "name (Z~A)",
        "Idle Rak", "Non-Idle Rak"
    )
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter By:",
                modifier = Modifier
                    .weight(0.4f)
                    .padding(horizontal = 5.dp),
                fontSize = 16.sp
            )

            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = !isExpanded },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = selectedFilter,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    textStyle = TextStyle(lineHeight = 24.sp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )

                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {
                    options.forEach { text ->
                        DropdownMenuItem(
                            text = { Text(text = text) },
                            onClick = {
                                onFilterChange(text)
                                isExpanded = false
                            }
                        )
                    }
                }
            }
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