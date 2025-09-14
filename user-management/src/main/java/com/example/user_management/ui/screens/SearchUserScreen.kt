package com.example.user_management.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.example.user_management.ui.components.UserManagementFloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// Firestore 数据类
data class Customer(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val postcode: String = "",
    val city: String = "",
    val state: String = ""
)

@Composable
fun SearchUserScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,


) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("name (A~Z)") }
    var customers by remember { mutableStateOf(listOf<Customer>()) }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<Customer>()) }

    // Firestore 实时监听
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val registration: ListenerRegistration = db.collection("customers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                customers = snapshot?.documents?.mapNotNull { doc ->
                    if (!doc.exists()) return@mapNotNull null

                    Customer(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        phone = doc.get("phone")?.toString() ?: "",
                        email = doc.getString("email") ?: "",
                        address = doc.getString("address") ?: "",
                        postcode = doc.get("postcode")?.toString() ?: "",
                        city = doc.getString("city") ?: "",
                        state = doc.getString("state") ?: ""
                    )
                } ?: emptyList()
            }

        onDispose { registration.remove() }
    }

    // 本地过滤逻辑
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
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                )
            }
        },
        floatingActionButton = {
            // 只有在非多选模式下才显示 FAB
            if (!isMultiSelectMode) {
                UserManagementFloatingActionButton(
                    navController = navController,
                    modifier = Modifier.padding(bottom = 2.dp, end = 2.dp),
                    onToggleMultiSelect = {
                        isMultiSelectMode = true
                        selectedItems = emptySet()
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
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
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredCustomers) { customer ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isMultiSelectMode) {
                                    Checkbox(
                                        checked = customer in selectedItems,
                                        onCheckedChange = { checked ->
                                            selectedItems = if (checked) {
                                                selectedItems + customer
                                            } else {
                                                selectedItems - customer
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                CustomerListItem(customer = customer) {
                                    if (isMultiSelectMode) {
                                        selectedItems = if (customer in selectedItems)
                                            selectedItems - customer
                                        else selectedItems + customer
                                    } else {
                                        navController.navigate("customer_detail/${customer.id}")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 多选工具栏固定在底部
            if (isMultiSelectMode) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Selected: ${selectedItems.size}", style = MaterialTheme.typography.bodyLarge)
                        Row {
                            TextButton(onClick = {
                                selectedItems = emptySet()
                                isMultiSelectMode = false
                            }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val db = FirebaseFirestore.getInstance()
                                    selectedItems.forEach { customer ->
                                        if (customer.id.isNotBlank()) {
                                            db.collection("customers")
                                                .document(customer.id)
                                                .delete()
                                        }
                                    }
                                    selectedItems = emptySet()
                                    isMultiSelectMode = false
                                },
                                enabled = selectedItems.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerListItem(customer: Customer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = customer.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            CardRow(label = "Phone", value = customer.phone)
            CardRow(label = "Email", value = customer.email)
            CardRow(label = "Address", value = customer.address)
        }
    }
}

@Composable
fun CardRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Label 固定宽度，保证冒号对齐
        Text(
            text = "$label:",
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier.width(90.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Value 可以换行
        Text(
            text = value,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
            softWrap = true
        )
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBy(
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    val options = listOf("name (A~Z)", "name (Z~A)")
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = selectedFilter,
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter By") },
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
