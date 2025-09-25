package com.example.delivery_and_transportation_management.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.viewmodel.AddTransportationViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransportationScreen(
    navController: NavController,
    deliveryViewModel: DeliveryViewModel = viewModel()
) {
    val vm: AddTransportationViewModel = viewModel()

    val plateNumber by vm.plateNumber.collectAsState()
    val driverList by vm.driverList.collectAsState()
    val selectedDriverName by vm.selectedDriverName.collectAsState()
    val selectedEmployeeID by vm.selectedEmployeeID.collectAsState()
    val expandedDriver by vm.expandedDriver.collectAsState()
    val selectedType by vm.selectedType.collectAsState()
    val expandedType by vm.expandedType.collectAsState()
    val vehicleTypes = vm.vehicleTypes

    // Load drivers once
    LaunchedEffect(Unit) { vm.loadDrivers() }

    val isPlateNumberValid = plateNumber.isBlank() || vm.isValidPlateNumber(plateNumber)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .then(if (isLandscape) Modifier.verticalScroll(rememberScrollState()) else Modifier),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = plateNumber,
            onValueChange = { vm.setPlateNumber(it) },
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
            onExpandedChange = { vm.toggleDriverExpanded() }
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
                onDismissRequest = { vm.dismissDriverDropdown() }
            ) {
                driverList.forEach { (name, employeeID) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            vm.selectDriver(name, employeeID)
                            vm.dismissDriverDropdown()
                        }
                    )
                }
            }
        }

        // Vehicle type dropdown
        ExposedDropdownMenuBox(
            expanded = expandedType,
            onExpandedChange = { vm.toggleTypeExpanded() }
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
                onDismissRequest = { vm.dismissTypeDropdown() }
            ) {
                vehicleTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = { vm.selectType(type); vm.dismissTypeDropdown() }
                    )
                }
            }
        }

        Button(
            onClick = {
                if (plateNumber.isNotBlank() &&
                    selectedDriverName.isNotBlank() &&
                    selectedEmployeeID.isNotBlank() &&
                    vm.isValidPlateNumber(plateNumber)
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
                    vm.isValidPlateNumber(plateNumber)
        ) { Text("Save") }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddTransportationScreen() {
    AddTransportationScreen(navController = rememberNavController())
}