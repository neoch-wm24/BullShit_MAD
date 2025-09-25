package com.example.delivery_and_transportation_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransportationScreen(
    navController: NavController,
    deliveryViewModel: DeliveryViewModel = viewModel()
) {
    var plateNumber by rememberSaveable { mutableStateOf("") }
    // (driverName, employeeID)
    var driverList by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var selectedDriverName by rememberSaveable { mutableStateOf("") }
    var selectedEmployeeID by rememberSaveable { mutableStateOf("") }
    var expandedDriver by rememberSaveable { mutableStateOf(false) }

    var expandedType by rememberSaveable { mutableStateOf(false) }
    var selectedType by rememberSaveable { mutableStateOf("Car") }
    val vehicleTypes = listOf("Car", "Van", "Truck", "Container Truck")

    // Load drivers (role == driver) retrieving name + employeeID
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("role", "driver")
            .get()
            .addOnSuccessListener { snapshot ->
                driverList = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val employeeID = doc.getString("employeeID") ?: return@mapNotNull null
                    name to employeeID
                }
            }
            .addOnFailureListener { driverList = emptyList() }
    }

    fun isValidPlateNumber(plate: String): Boolean =
        plate.matches(Regex("^[A-Za-z]{1,3}[0-9]{1,4}$"))
    val isPlateNumberValid = plateNumber.isBlank() || isValidPlateNumber(plateNumber)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        // Driver dropdown (shows name but stores employeeID)
        ExposedDropdownMenuBox(
            expanded = expandedDriver,
            onExpandedChange = { expandedDriver = !expandedDriver }
        ) {
            OutlinedTextField(
                value = selectedDriverName,
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
                driverList.forEach { (name, employeeID) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            selectedDriverName = name
                            selectedEmployeeID = employeeID
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
                        onClick = { selectedType = type; expandedType = false }
                    )
                }
            }
        }

        Button(
            onClick = {
                if (plateNumber.isNotBlank() &&
                    selectedDriverName.isNotBlank() &&
                    selectedEmployeeID.isNotBlank() &&
                    isValidPlateNumber(plateNumber)
                ) {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val newDelivery = Delivery(
                        id = UUID.randomUUID().toString(),
                        employeeID = selectedEmployeeID,
                        driverName = selectedDriverName,
                        type = selectedType,
                        date = today,
                        plateNumber = plateNumber,
                        stops = emptyList(),
                        assignedOrders = emptyList()
                    )
                    deliveryViewModel.addDelivery(newDelivery)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = plateNumber.isNotBlank() &&
                    selectedDriverName.isNotBlank() &&
                    selectedEmployeeID.isNotBlank() &&
                    isValidPlateNumber(plateNumber)
        ) { Text("Save") }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddTransportationScreen() {
    AddTransportationScreen(navController = rememberNavController())
}