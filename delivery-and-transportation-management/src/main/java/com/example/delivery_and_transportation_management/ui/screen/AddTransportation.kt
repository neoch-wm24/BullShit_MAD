package com.example.delivery_and_transportation_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.delivery_and_transportation_management.data.Delivery
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransportationScreen(
    navController: NavController,
    onSave: (Delivery) -> Unit
) {
    var plateNumber by rememberSaveable { mutableStateOf("") }
    var driverName by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedType by rememberSaveable { mutableStateOf("Car") }

    val vehicleTypes = listOf("Car", "Van", "Air", "Sea")

    // Validation function for plate number
    fun isValidPlateNumber(plate: String): Boolean {
        val regex = Regex("^[A-Za-z]{1,3}[0-9]{1,4}$")
        return plate.matches(regex)
    }

    val isPlateNumberValid = plateNumber.isBlank() || isValidPlateNumber(plateNumber)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transportation") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = plateNumber,
                onValueChange = { plateNumber = it.uppercase() }, // Convert to uppercase for consistency
                label = { Text("Plate Number") },
                placeholder = { Text("e.g., A123 or ABC123") },
                isError = !isPlateNumberValid,
                supportingText = {
                    if (!isPlateNumberValid) {
                        Text(
                            "Plate number must be 1-3 letters followed by 1-4 numbers (e.g., A123, ABC123)",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("Format: 1-3 letters + 1-4 numbers")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = driverName,
                onValueChange = { driverName = it },
                label = { Text("Driver Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // 🚚 Vehicle Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Vehicle Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    vehicleTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (plateNumber.isNotBlank() && driverName.isNotBlank() && isValidPlateNumber(plateNumber)) {
                        val newDelivery = Delivery(
                            id = UUID.randomUUID().toString(),
                            driverName = driverName,
                            type = selectedType,
                            date = "", // 🚫 no date here, schedule will set it later
                            plateNumber = plateNumber
                        )
                        onSave(newDelivery)
                        // Don't navigate here - let the navigation host handle it
                    }
                },
                modifier = Modifier.align(Alignment.End),
                enabled = plateNumber.isNotBlank() && driverName.isNotBlank() && isValidPlateNumber(plateNumber)
            ) {
                Text("Save")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddTransportationScreen() {
    AddTransportationScreen(
        navController = rememberNavController(),
        onSave = {}
    )
}
