package com.example.warehouse_management.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.core_data.RackInfo
import com.example.core_data.RackManager
import com.example.core_ui.components.FilterBy
import com.example.core_ui.components.SearchBar
import com.example.warehouse_management.ui.components.FloatingActionButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRackScreen(
    navController: NavHostController,
    onNavigateToRackInfo: ((String) -> Unit)? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("name (A~Z)") }

    // Multi-select state
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedRacks by remember { mutableStateOf(setOf<RackInfo>()) }
    var isDeleting by remember { mutableStateOf(false) }

    val rackList = RackManager.rackList

    val filteredRackList = remember(rackList, searchText, selectedFilter) {
        var result = if (searchText.isBlank()) {
            rackList
        } else {
            rackList.filter { rack ->
                rack.name.lowercase().startsWith(searchText.lowercase())
            }
        }

        result = when (selectedFilter) {
            "name (A~Z)" -> result.sortedBy { it.name.lowercase() }
            "name (Z~A)" -> result.sortedByDescending { it.name.lowercase() }
            "Idle Rack" -> result.filter { it.state.equals("Idle", ignoreCase = true) }
            "Non-Idle Rack" -> result.filter { !it.state.equals("Idle", ignoreCase = true) }
            else -> result
        }

        result
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // 搜索栏
            SearchBar(
                value = searchText,
                onValueChange = { searchText = it },
                label = "Search Rack",
                placeholder = "Rack Name",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ 使用 core_ui 的 FilterBy
            FilterBy(
                selectedFilter = selectedFilter,
                options = listOf("name (A~Z)", "name (Z~A)", "Idle Rack", "Non-Idle Rack"),
                onFilterChange = { newFilter ->
                    selectedFilter = newFilter
                    scope.launch {
                        snackbarHostState.showSnackbar("Filter changed to: $newFilter")
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 列表（每一项有边框 ✅）
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = if (isMultiSelectMode) 60.dp else 0.dp)
            ) {
                if (filteredRackList.isEmpty()) {
                    item {
                        Text(
                            text = if (searchText.isBlank()) {
                                "No Rack available.\nAdd a new Rack from the add Rack page."
                            } else {
                                "No Rack found matching \"$searchText\".\nTry a different search term."
                            },
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        )
                    }
                } else {
                    items(filteredRackList) { rack: RackInfo ->
                        // Row wrapper to optionally show checkbox
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                                .border(2.dp, Color(0xFFFF69B4))
                                .background(Color(0xFFF5F5F5))
                                .padding(12.dp)
                                .clickable {
                                    if (isMultiSelectMode) {
                                        selectedRacks = if (selectedRacks.contains(rack)) {
                                            selectedRacks - rack
                                        } else {
                                            selectedRacks + rack
                                        }
                                    } else {
                                        try {
                                            if (onNavigateToRackInfo != null) {
                                                onNavigateToRackInfo(rack.id)
                                            } else {
                                                navController.navigate("RackDetails/${rack.id}")
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("SearchRack", "Navigation failed", e)
                                        }
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isMultiSelectMode) {
                                Checkbox(
                                    checked = selectedRacks.contains(rack),
                                    onCheckedChange = { checked ->
                                        selectedRacks = if (checked) selectedRacks + rack else selectedRacks - rack
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rack.name,
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Layer: ${rack.layer}",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "State: ${rack.state}",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
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
                    selectedRacks = emptySet()
                }
            )
        }

        if (isMultiSelectMode) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 8.dp, end = 8.dp, bottom = 25.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = if (isDeleting) "Deleting... ${selectedRacks.size}" else "Selected: ${selectedRacks.size}")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            if (!isDeleting) {
                                selectedRacks = emptySet()
                                isMultiSelectMode = false
                            }
                        }, enabled = !isDeleting) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                if (selectedRacks.isNotEmpty() && !isDeleting) {
                                    isDeleting = true
                                    scope.launch {
                                        // simulate small delay for UX parity
                                        try {
                                            val ids = selectedRacks.map { it.id }
                                            RackManager.removeRacks(ids)
                                        } finally {
                                            // reset state
                                            isDeleting = false
                                            selectedRacks = emptySet()
                                            isMultiSelectMode = false
                                        }
                                    }
                                }
                            },
                            enabled = selectedRacks.isNotEmpty() && !isDeleting
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = if (isDeleting) Color.Gray else Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchRackScreenPreview() {
    val navController = rememberNavController()
    SearchRackScreen(navController = navController)
}