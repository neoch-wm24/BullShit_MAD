package com.example.warehouse_management.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.core_data.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRakScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var newRakName by remember { mutableStateOf("") }
    var selectedLayer by remember { mutableIntStateOf(1) }
    var selectedState by remember { mutableStateOf("Idle") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Add New Rak") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            try {
                                navController.popBackStack()
                            } catch (e: Exception) {
                                // Handle navigation error gracefully
                                println("Navigation error: ${e.message}")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                RakInputForm(
                    rakName = newRakName,
                    onRakNameChange = { newRakName = it },
                    selectedLayer = selectedLayer,
                    onSelectedLayerChange = { selectedLayer = it },
                    selectedState = selectedState,
                    onSelectedStateChange = { selectedState = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )

                AddCancelButtons(
                    onAddClick = {
                        // Validate input before adding
                        if (newRakName.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please enter a Rak name")
                            }
                            return@AddCancelButtons
                        }

                        try {
                            // Save the new Rak name to RakManager
                            val newRak = RakInfo(
                                id = UUID.randomUUID().toString(),
                                name = newRakName.trim(),
                                layer = selectedLayer,
                                state = selectedState
                            )
                            RakManager.addRak(newRak)

                            scope.launch {
                                snackbarHostState.showSnackbar("Rak added successfully!")
                                // Small delay to show the snackbar before navigating
                                kotlinx.coroutines.delay(500)
                                try {
                                    // Navigate to searchrak instead of popping back stack
                                    navController.navigate("searchrak") {
                                        // Clear the back stack to prevent multiple instances
                                        popUpTo("searchrak") { inclusive = true }
                                    }
                                } catch (navError: Exception) {
                                    println("Navigation error after adding rak: ${navError.message}")
                                    // Fallback to popBackStack if navigate fails
                                    navController.popBackStack()
                                }
                            }
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error adding Rak: ${e.message}")
                            }
                            println("Error adding Rak: ${e.message}")
                        }
                    },
                    onCancelClick = {
                        try {
                            // Navigate back to searchrak on cancel
                            navController.navigate("searchrak") {
                                popUpTo("searchrak") { inclusive = true }
                            }
                        } catch (e: Exception) {
                            println("Navigation error on cancel: ${e.message}")
                            // Fallback to popBackStack
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RakInputForm(
    rakName: String,
    onRakNameChange: (String) -> Unit,
    selectedLayer: Int,
    onSelectedLayerChange: (Int) -> Unit,
    selectedState: String,
    onSelectedStateChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var layerExpanded by remember { mutableStateOf(false) }
    var stateExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Rak Name Field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Rak Name:",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(80.dp),
                fontSize = 16.sp
            )

            OutlinedTextField(
                value = rakName,
                onValueChange = onRakNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                placeholder = { Text("Enter rak name") }
            )
        }

        // Layers Field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Layer:",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(80.dp),
                fontSize = 16.sp
            )

            ExposedDropdownMenuBox(
                expanded = layerExpanded,
                onExpandedChange = { layerExpanded = !layerExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedLayer.toString(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = layerExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = layerExpanded,
                    onDismissRequest = { layerExpanded = false }
                ) {
                    listOf(1, 2, 3, 4, 5).forEach { layer ->
                        DropdownMenuItem(
                            text = { Text(text = layer.toString()) },
                            onClick = {
                                onSelectedLayerChange(layer)
                                layerExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Rak State field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Rak State:",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(80.dp),
                fontSize = 16.sp
            )

            ExposedDropdownMenuBox(
                expanded = stateExpanded,
                onExpandedChange = { stateExpanded = !stateExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedState,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = stateExpanded,
                    onDismissRequest = { stateExpanded = false }
                ) {
                    listOf("Idle", "Non-Idle").forEach { state ->
                        DropdownMenuItem(
                            text = { Text(text = state) },
                            onClick = {
                                onSelectedStateChange(state)
                                stateExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCancelButtons(
    onAddClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onAddClick,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Add", fontSize = 16.sp)
        }

        Button(
            onClick = onCancelClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD05667),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Cancel", fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddRakScreenPreview() {
    val navController = rememberNavController()
    AddRakScreen(navController = navController)
}