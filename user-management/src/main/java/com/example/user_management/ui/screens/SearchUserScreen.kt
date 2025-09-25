package com.example.user_management.ui.screens

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.user_management.ui.components.UserManagementFloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.user_management.viewmodel.SearchUserViewModel

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
    // ViewModel to persist UI state across rotation
    val uiViewModel: SearchUserViewModel = viewModel()
    val searchQuery by uiViewModel.searchQuery.collectAsState()
    val selectedFilter by uiViewModel.selectedFilter.collectAsState()
    val isMultiSelectMode by uiViewModel.isMultiSelectMode.collectAsState()
    val selectedIds by uiViewModel.selectedIds.collectAsState()

    var customers by remember { mutableStateOf(listOf<Customer>()) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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

    Box(modifier = modifier.fillMaxSize()) {
        if (isLandscape) {
            // 横屏：整个页面（搜索栏 + 筛选 + 列表）统一滚动
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { uiViewModel.setSearchQuery(it) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
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
                        Spacer(modifier = Modifier.height(4.dp))
                        FilterBy(
                            selectedFilter = selectedFilter,
                            onFilterChange = { uiViewModel.setSelectedFilter(it) }
                        )
                    }

                    if (filteredCustomers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isBlank()) "暂无用户，请点击右下角 + 按钮添加" else "没有找到匹配的用户",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(filteredCustomers, key = { it.id }) { customer ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isMultiSelectMode) {
                                    Checkbox(
                                        checked = customer.id in selectedIds,
                                        onCheckedChange = { checked -> uiViewModel.setItemChecked(customer.id, checked) }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                CustomerListItem(customer = customer) {
                                    if (isMultiSelectMode) uiViewModel.toggleItem(customer.id) else navController.navigate("CustomerDetails/${customer.id}")
                                }
                            }
                        }
                    }
                }

                if (isMultiSelectMode) {
                    // 底部多选工具栏
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Selected: ${selectedIds.size}", style = MaterialTheme.typography.bodyLarge)
                            Row {
                                TextButton(onClick = { uiViewModel.exitMultiSelect() }) { Text("Cancel") }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        val db = FirebaseFirestore.getInstance()
                                        selectedIds.forEach { id ->
                                            if (id.isNotBlank()) {
                                                db.collection("customers").document(id).delete()
                                            }
                                        }
                                        uiViewModel.exitMultiSelect()
                                    },
                                    enabled = selectedIds.isNotEmpty()
                                ) { Icon(Icons.Default.Delete, contentDescription = "Delete Selected") }
                            }
                        }
                    }
                }

                if (!isMultiSelectMode) {
                    UserManagementFloatingActionButton(
                        navController = navController,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        onToggleMultiSelect = { uiViewModel.enterMultiSelect() }
                    )
                }
            }
        } else {
            // 竖屏：沿用原先 Scaffold，只列表滚动
            Scaffold(
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { uiViewModel.setSearchQuery(it) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
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
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        FilterBy(selectedFilter = selectedFilter, onFilterChange = { uiViewModel.setSelectedFilter(it) })
                        if (filteredCustomers.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isBlank()) "暂无用户，请点击右下角 + 按钮添加" else "没有找到匹配的用户",
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
                                items(filteredCustomers, key = { it.id }) { customer ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (isMultiSelectMode) {
                                            Checkbox(
                                                checked = customer.id in selectedIds,
                                                onCheckedChange = { checked -> uiViewModel.setItemChecked(customer.id, checked) }
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                        }
                                        CustomerListItem(customer = customer) {
                                            if (isMultiSelectMode) uiViewModel.toggleItem(customer.id) else navController.navigate("CustomerDetails/${customer.id}")
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                                Text("Selected: ${selectedIds.size}", style = MaterialTheme.typography.bodyLarge)
                                Row {
                                    TextButton(onClick = { uiViewModel.exitMultiSelect() }) { Text("Cancel") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            val db = FirebaseFirestore.getInstance()
                                            selectedIds.forEach { id ->
                                                if (id.isNotBlank()) db.collection("customers").document(id).delete()
                                            }
                                            uiViewModel.exitMultiSelect()
                                        },
                                        enabled = selectedIds.isNotEmpty()
                                    ) { Icon(Icons.Default.Delete, contentDescription = "Delete Selected") }
                                }
                            }
                        }
                    }
                    if (!isMultiSelectMode) {
                        UserManagementFloatingActionButton(
                            navController = navController,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            onToggleMultiSelect = { uiViewModel.enterMultiSelect() }
                        )
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
