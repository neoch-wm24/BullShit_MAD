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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRackScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var newRackName by remember { mutableStateOf("") }
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
            verticalArrangement = Arrangement.Top
        ) {
            RackInputForm(
                rackName = newRackName,
                onRackNameChange = { newRackName = it },
                selectedLayer = selectedLayer,
                onSelectedLayerChange = { selectedLayer = it },
                selectedState = selectedState,
                onSelectedStateChange = { selectedState = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            AddButton(
                onAddClick = {
                    if (newRackName.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter a Rack name")
                        }
                        return@AddButton
                    }

                    scope.launch {
                        try {
                            val rackId = UUID.randomUUID().toString()
                            val newRack = RackInfo(
                                id = rackId,
                                name = newRackName.trim(),
                                layer = selectedLayer,
                                state = selectedState
                            )

                            // ✅ 调用 RackManager 保存到 Firestore
                            RackManager.addRack(newRack)

                            snackbarHostState.showSnackbar("Rack added successfully!")
                            kotlinx.coroutines.delay(100)
                            try {
                                navController.navigate("searchrack") {
                                    popUpTo("searchrack") { inclusive = true }
                                }
                            } catch (navError: Exception) {
                                println("Navigation error after adding rack: ${navError.message}")
                                navController.popBackStack()
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Error adding Rack: ${e.message}")
                            println("Error adding Rack: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RackInputForm(
    rackName: String,
    onRackNameChange: (String) -> Unit,
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

        // Rack Name Field
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
                value = rackName,
                onValueChange = onRackNameChange,
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

        // Rack State field
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
fun AddRackScreenPreview() {
    val navController = rememberNavController()
    AddRackScreen(navController = navController)
}