package com.example.user_management.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.core_data.CustomerInfo
import com.example.core_data.CustomerManager
import com.example.user_management.ui.components.UserManagementFloatingActionButton

@Composable
fun SearchUserScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("name (A~Z)") }

    val customers = remember { CustomerManager.customerList }

    // 过滤逻辑
    val filteredCustomers = remember(customers, searchQuery, selectedFilter) {
        var result = if (searchQuery.isBlank()) {
            customers
        } else {
            customers.filter { customer ->
                customer.name.contains(searchQuery, ignoreCase = true) ||
                        customer.phone.contains(searchQuery, ignoreCase = true) ||
                        customer.email.contains(searchQuery, ignoreCase = true)
            }
        }

        result = when (selectedFilter) {
            "name (A~Z)" -> result.sortedBy { it.name.lowercase() }
            "name (Z~A)" -> result.sortedByDescending { it.name.lowercase() }
            else -> result
        }
        result
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    label = { Text("Search Users") },
                    placeholder = { Text("Search by name, phone or email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        floatingActionButton = {
            UserManagementFloatingActionButton(
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
            // 筛选下拉菜单
            FilterBy(
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it }
            )

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "暂无用户，请点击右下角 + 按钮添加"
                        else "没有找到匹配的用户",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCustomers) { customer ->
                        CustomerListItem(
                            customer = customer,
                            onClick = {
                                navController.navigate("customer_detail/${customer.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerListItem(customer: CustomerInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("用户: ${customer.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("电话: ${customer.phone}", fontSize = 14.sp)
            Text("邮箱: ${customer.email}", fontSize = 14.sp)
            Text(
                "地址: ${customer.address}, ${customer.city}, ${customer.state}, ${customer.postcode}",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // ✅ 解决 experimental 警告
@Composable
private fun FilterBy(
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    val options = listOf("name (A~Z)", "name (Z~A)")
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    textStyle = TextStyle(fontSize = 14.sp),
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
