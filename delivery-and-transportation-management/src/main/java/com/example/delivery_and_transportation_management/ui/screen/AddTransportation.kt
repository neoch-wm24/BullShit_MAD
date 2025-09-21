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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransportationScreen(
    navController: NavController,
    deliveryViewModel: DeliveryViewModel = viewModel()
) {
    var plateNumber by rememberSaveable { mutableStateOf("") }
    var driverList by remember { mutableStateOf(listOf<String>()) }
    var selectedDriver by rememberSaveable { mutableStateOf("") }
    var expandedDriver by rememberSaveable { mutableStateOf(false) }

    var expandedType by rememberSaveable { mutableStateOf(false) }
    var selectedType by rememberSaveable { mutableStateOf("Car") }
    val vehicleTypes = listOf("Car", "Van", "Truck", "Container Truck")

    // 加载司机
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("role", "driver")
            .get()
            .addOnSuccessListener { snapshot ->
                driverList = snapshot.documents.mapNotNull { it.getString("name") }
            }
            .addOnFailureListener {
                driverList = emptyList()
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
        // Plate 输入
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
                } else {
                    Text("Format: 1-3 letters + 1-4 numbers")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Driver 下拉
        ExposedDropdownMenuBox(
            expanded = expandedDriver,
            onExpandedChange = { expandedDriver = !expandedDriver }
        ) {
            OutlinedTextField(
                value = selectedDriver,
                onValueChange = {},
                readOnly = true,
                label = { Text("Driver") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDriver)
                },
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

        // Type 下拉
        ExposedDropdownMenuBox(
            expanded = expandedType,
            onExpandedChange = { expandedType = !expandedType }
        ) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Vehicle Type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType)
                },
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

        Button(
            onClick = {
                if (plateNumber.isNotBlank() &&
                    selectedDriver.isNotBlank() &&
                    isValidPlateNumber(plateNumber)
                ) {
                    val newDelivery = Delivery(
                        id = UUID.randomUUID().toString(),
                        driverName = selectedDriver,
                        type = selectedType,
                        date = "", // 用户可加一个日期选择
                        plateNumber = plateNumber,
                        stops = emptyList()
                    )
                    deliveryViewModel.addDelivery(newDelivery)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = plateNumber.isNotBlank() &&
                    selectedDriver.isNotBlank() &&
                    isValidPlateNumber(plateNumber)
        ) {
            Text("Save")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddTransportationScreen() {
    AddTransportationScreen(navController = rememberNavController())
}
