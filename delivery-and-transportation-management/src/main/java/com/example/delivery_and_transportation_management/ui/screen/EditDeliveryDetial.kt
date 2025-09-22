package com.example.delivery_and_transportation_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeliveryScreen(
    delivery: Delivery,
    navController: NavController,
    onSave: (Delivery) -> Unit
) {
    // States matching AddTransportation layout
    var plateNumber by remember { mutableStateOf(delivery.plateNumber.orEmpty()) }

    var driverList by remember { mutableStateOf(listOf<String>()) }
    var selectedDriver by remember { mutableStateOf(delivery.driverName) }
    var expandedDriver by remember { mutableStateOf(false) }

    var expandedType by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(delivery.type.ifBlank { "Car" }) }
    val vehicleTypes = listOf("Car", "Van", "Truck", "Container Truck")

    // Load drivers (same as AddTransportation)
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("role", "driver")
            .get()
            .addOnSuccessListener { snapshot ->
                driverList = snapshot.documents.mapNotNull { it.getString("name") }
                // Ensure currently selected driver remains even if not in fetched list
                if (selectedDriver.isNotBlank() && selectedDriver !in driverList) {
                    driverList = listOf(selectedDriver) + driverList
                }
            }
            .addOnFailureListener {
                // Keep current driver if fetch fails
                driverList = if (selectedDriver.isNotBlank()) listOf(selectedDriver) else emptyList()
            }
    }

    fun isValidPlateNumber(plate: String): Boolean {
        val regex = Regex("^[A-Za-z]{1,3}[0-9]{1,4}$")
        return plate.matches(regex)
    }
    val isPlateNumberValid = plateNumber.isBlank() || isValidPlateNumber(plateNumber)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Plate Number
        OutlinedTextField(
            value = plateNumber,
            onValueChange = { plateNumber = it.uppercase() },
            label = { Text("Plate Number") },
            placeholder = { Text("e.g., A123 or ABC123") },
            isError = !isPlateNumberValid,
            supportingText = {
                if (!isPlateNumberValid) {
                    Text(
                        "Plate number must be 1-3 letters followed by 1-4 numbers (e.g., A123, ABC123)",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Driver dropdown
        ExposedDropdownMenuBox(
            expanded = expandedDriver,
            onExpandedChange = { expandedDriver = !expandedDriver }
        ) {
            OutlinedTextField(
                value = selectedDriver,
                onValueChange = {},
                readOnly = true,
                label = { Text("Driver") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDriver) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedDriver,
                onDismissRequest = { expandedDriver = false }
            ) {
                driverList.forEach { driver ->
                    DropdownMenuItem(
                        text = { Text(driver) },
                        onClick = {
                            selectedDriver = driver
                            expandedDriver = false
                        }
                    )
                }
            }
        }

        // Vehicle type dropdown
        ExposedDropdownMenuBox(
            expanded = expandedType,
            onExpandedChange = { expandedType = !expandedType }
        ) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Vehicle Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedType,
                onDismissRequest = { expandedType = false }
            ) {
                vehicleTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            selectedType = type
                            expandedType = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (plateNumber.isNotBlank() && selectedDriver.isNotBlank() && isValidPlateNumber(plateNumber)) {
                    val updatedDelivery = delivery.copy(
                        plateNumber = plateNumber,
                        driverName = selectedDriver,
                        type = selectedType
                        // date remains unchanged to match AddTransportation layout (no date field)
                    )
                    onSave(updatedDelivery)
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = plateNumber.isNotBlank() && selectedDriver.isNotBlank() && isValidPlateNumber(plateNumber)
        ) {
            Text("Save")
        }
    }
}
