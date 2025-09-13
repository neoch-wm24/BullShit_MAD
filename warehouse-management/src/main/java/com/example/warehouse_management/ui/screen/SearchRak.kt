package com.example.warehouse_management.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.core_data.RakInfo
import com.example.core_data.RakManager
import com.example.core_ui.components.SearchBar
import kotlinx.coroutines.launch
import com.example.warehouse_management.ui.components.FloatingActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRakScreen(
    navController: NavHostController,
    onNavigateToRakInfo: ((String) -> Unit)? = null // Add callback for navigation
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var searchText by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var selectedFilter by remember { mutableStateOf("name (A~Z)") }

    val rakList = RakManager.rakList

    // Filter and sort the rak list based on search text and selected filter
    val filteredRakList = remember(rakList, searchText, selectedFilter) {
        var result = if (searchText.isBlank()) {
            rakList
        } else {
            rakList.filter { rak ->
                rak.name.lowercase().startsWith(searchText.lowercase())
            }
        }
        
        // Apply filter/sorting based on selected filter
        result = when (selectedFilter) {
            "name (A~Z)" -> result.sortedBy { it.name.lowercase() }
            "name (Z~A)" -> result.sortedByDescending { it.name.lowercase() }
            "Idle Rak" -> result.filter { it.state.equals("Idle", ignoreCase = true) }
            "Non-Idle Rak" -> result.filter { !it.state.equals("Idle", ignoreCase = true) }
            else -> result
        }
        
        result
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
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
            FilterBy(
                selectedFilter = selectedFilter,
                onFilterChange = { newFilter ->
                    selectedFilter = newFilter
                    scope.launch {
                        snackbarHostState.showSnackbar("Filter changed to: $newFilter")
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (filteredRakList.isEmpty()) {
                    item {
                        Text(
                            text = if (searchText.isBlank()) {
                                "No Rak available.\nAdd a new Rak from the Add Rak page."
                            } else {
                                "No Rak found matching \"$searchText\".\nTry a different search term."
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
                    items(filteredRakList) { rak: RakInfo ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                                .background(Color(0xFFF5F5F5))
                                .padding(12.dp)
                                .clickable {
                                    android.util.Log.d("SearchRak", "Navigating to rak_information/${rak.id}")
                                    android.util.Log.d("SearchRak", "RakManager has ${RakManager.rakList.size} items")
                                    android.util.Log.d("SearchRak", "Rak exists: ${RakManager.getRakById(rak.id) != null}")

                                    try {
                                        // Use callback if provided, otherwise use navController directly
                                        if (onNavigateToRakInfo != null) {
                                            onNavigateToRakInfo(rak.id)
                                        } else {
                                            navController.navigate("rak_information/${rak.id}")
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("SearchRak", "Navigation failed", e)
                                        // Note: Toast needs context, but we'll handle this in the calling composable
                                        // since we can't easily get context here without changing the function signature
                                    }
                                }
                        ) {
                            Text(
                                text = "RakName: ${rak.name}",
                                fontSize = 20.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Layer: ${rak.layer}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "Rak State: ${rak.state}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            navController = navController,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
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
        Column {
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchRakScreenPreview() {
    val navController = rememberNavController()
    SearchRakScreen(navController = navController)
}
