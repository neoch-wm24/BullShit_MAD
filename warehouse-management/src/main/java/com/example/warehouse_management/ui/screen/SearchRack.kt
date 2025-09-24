package com.example.warehouse_management.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    // ✅ 直接用 RackManager 的 rackList（自动监听 Firestore）
    val rackList by remember { derivedStateOf { RackManager.rackList } }

    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedRacks by remember { mutableStateOf(setOf<RackInfo>()) }
    var isDeleting by remember { mutableStateOf(false) }

    val filteredRackList = remember(rackList, searchText, selectedFilter) {
        var result = if (searchText.isBlank()) rackList
        else rackList.filter { it.name.lowercase().startsWith(searchText.lowercase()) }

        result = when (selectedFilter) {
            "name (A~Z)" -> result.sortedBy { it.name.lowercase() }
            "name (Z~A)" -> result.sortedByDescending { it.name.lowercase() }
            "Idle Rack" -> result.filter { it.state.equals("Idle", true) }
            "Non-Idle Rack" -> result.filter { !it.state.equals("Idle", true) }
            else -> result
        }
        result
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            SearchBar(
                value = searchText,
                onValueChange = { searchText = it },
                label = "Search Rack",
                placeholder = "Rack Name",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

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

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = if (isMultiSelectMode) 60.dp else 0.dp)
            ) {
                if (filteredRackList.isEmpty()) {
                    item {
                        Text(
                            text = if (searchText.isBlank()) {
                                "No Rack available.\nAdd a new Rack from the add Rack page."
                            } else {
                                "No Rack found matching \"$searchText\"."
                            },
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        )
                    }
                } else {
                    items(filteredRackList) { rack ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                                .border(2.dp, Color(0xFFFF69B4))
                                .background(Color(0xFFF5F5F5))
                                .padding(12.dp)
                                .clickable {
                                    if (isMultiSelectMode) {
                                        selectedRacks = if (selectedRacks.contains(rack)) selectedRacks - rack
                                        else selectedRacks + rack
                                    } else {
                                        onNavigateToRackInfo?.invoke(rack.id)
                                            ?: navController.navigate("RackDetails/${rack.id}")
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
                                Text(rack.name, fontSize = 20.sp, color = Color.Black)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Layer: ${rack.layer}", fontSize = 14.sp, color = Color.DarkGray)
                                    Text("State: ${rack.state}", fontSize = 14.sp, color = Color.DarkGray)
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
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                onToggleMultiSelect = {
                    isMultiSelectMode = true
                    selectedRacks = emptySet()
                }
            )
        }

        if (isMultiSelectMode) {
            Card(
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 8.dp, end = 8.dp, bottom = 25.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDeleting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isDeleting) "Deleting... ${selectedRacks.size}"
                            else "Selected: ${selectedRacks.size}"
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = {
                                if (!isDeleting) {
                                    selectedRacks = emptySet()
                                    isMultiSelectMode = false
                                }
                            },
                            enabled = !isDeleting
                        ) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                if (selectedRacks.isNotEmpty() && !isDeleting) {
                                    isDeleting = true
                                    scope.launch {
                                        try {
                                            selectedRacks.forEach { rack ->
                                                RackManager.removeRack(rack.id)
                                            }
                                        } finally {
                                            isDeleting = false
                                            selectedRacks = emptySet()
                                            isMultiSelectMode = false
                                        }
                                    }
                                }
                            },
                            enabled = selectedRacks.isNotEmpty() && !isDeleting
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = if (isDeleting) Color.Gray else Color.Black
                            )
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
