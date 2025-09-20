package com.example.warehouse_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top  // 👈 普通排列
        ) {
            RakInputForm(
                rakName = newRakName,
                onRakNameChange = { newRakName = it },
                selectedLayer = selectedLayer,
                onSelectedLayerChange = { selectedLayer = it },
                selectedState = selectedState,
                onSelectedStateChange = { selectedState = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp)) // 👈 表单和按钮间距

            AddButton(
                onAddClick = {
                    if (newRakName.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter a Rak name")
                        }
                        return@AddButton
                    }

                    try {
                        val newRak = RakInfo(
                            id = UUID.randomUUID().toString(),
                            name = newRakName.trim(),
                            layer = selectedLayer,
                            state = selectedState
                        )
                        RakManager.addRak(newRak)

                        scope.launch {
                            snackbarHostState.showSnackbar("Rak added successfully!")
                            kotlinx.coroutines.delay(500)
                            try {
                                navController.navigate("searchrak") {
                                    popUpTo("searchrak") { inclusive = true }
                                }
                            } catch (navError: Exception) {
                                println("Navigation error after adding rak: ${navError.message}")
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RakInputForm(
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val labelWidth = 100.dp // ✅ 统一 label 宽度

        // Rak Name Field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Rack Name:",
                modifier = Modifier.width(labelWidth),
                fontSize = 16.sp
            )

            OutlinedTextField(
                value = rakName,
                onValueChange = onRakNameChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                placeholder = { Text("Enter Rack Name") }
            )
        }

        // Layers Field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Layer:",
                modifier = Modifier.width(labelWidth),
                fontSize = 16.sp
            )

            ExposedDropdownMenuBox(
                expanded = layerExpanded,
                onExpandedChange = { layerExpanded = !layerExpanded },
                modifier = Modifier.weight(1f)
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
                    listOf(1, 2, 3).forEach { layer ->
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
                text = "Rack State:",
                modifier = Modifier.width(labelWidth),
                fontSize = 16.sp
            )

            ExposedDropdownMenuBox(
                expanded = stateExpanded,
                onExpandedChange = { stateExpanded = !stateExpanded },
                modifier = Modifier.weight(1f)
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
private fun AddButton(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onAddClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF69B4),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("Add", fontSize = 16.sp)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddRakScreenPreview() {
    val navController = rememberNavController()
    AddRakScreen(navController = navController)
}