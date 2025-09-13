package com.example.order_and_parcel_management.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.core_ui.components.BottomNavBar
import com.example.core_ui.components.PageTitleBar
import com.example.core_ui.theme.LogisticManagementApplicationTheme
import com.example.order_and_parcel_management.ui.components.FloatingActionButton

@Composable
fun SearchOrderAndParcelScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("name (A~Z)") }
    var isSearchActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
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

        // Content area (you can add your search results here)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // TODO: Add your search results list here
            // This is where you would display filtered/searched results
            if (searchText.isNotEmpty()) {
                Text(
                    text = "Searching for: \"$searchText\"\nFiltered by: $selectedFilter",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = Color.Gray
                )
            } else {
                Text(
                    text = "Enter search terms above\nFilter: $selectedFilter",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = Color.Gray
                )
            }
        }

        // Floating Action Button positioned at bottom end
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            FloatingActionButton(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    isSearchActive: Boolean
) {
    Box(
        modifier = Modifier
            .padding(20.dp)
            .background(Color(0xFFFFFFFF))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                placeholder = { Text("Search") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )

            if (isSearchActive && searchText.isNotBlank()) {
                IconButton(
                    onClick = onClearSearch,
                    modifier = Modifier.padding(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Search",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray
                    )
                }
            }
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
private fun OrderAndParcelWithNavigationPreview() {
    LogisticManagementApplicationTheme {
        val navController = rememberNavController()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8)),
            topBar = {
                PageTitleBar(navController = navController)
            },
            bottomBar = {
                BottomNavBar(navController = navController)
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