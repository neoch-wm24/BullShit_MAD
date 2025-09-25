package com.example.delivery_and_transportation_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.viewmodel.EditDeliveryViewModel
import android.content.res.Configuration
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeliveryScreen(
    delivery: Delivery,
    navController: NavController,
    onSave: (Delivery) -> Unit
) {
    val vm: EditDeliveryViewModel = viewModel()
    // Initialize once with incoming delivery data
    LaunchedEffect(delivery.id) {
        vm.initializeIfNeeded(
            initialPlate = delivery.plateNumber.orEmpty(),
            initialDriver = delivery.driverName,
            initialType = delivery.type
        )
        vm.loadDriversIfNeeded()
    }

    val plateNumber by vm.plateNumber.collectAsState()
    val driverList by vm.driverList.collectAsState()
    val selectedDriver by vm.selectedDriver.collectAsState()
    val expandedDriver by vm.expandedDriver.collectAsState()
    val selectedType by vm.selectedType.collectAsState()
    val expandedType by vm.expandedType.collectAsState()
    val vehicleTypes = vm.vehicleTypes

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val isPlateNumberValid = plateNumber.isBlank() || vm.isValidPlateNumber(plateNumber)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .then(if (isLandscape) Modifier.verticalScroll(rememberScrollState()) else Modifier),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Plate Number
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
                value = selectedDriver,
                onValueChange = {},
                readOnly = true,
                label = { Text("Driver") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDriver) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedDriver,
                onDismissRequest = { vm.dismissDriverDropdown() }
            ) {
                driverList.forEach { driver ->
                    DropdownMenuItem(
                        text = { Text(driver) },
                        onClick = {
                            vm.setSelectedDriver(driver)
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
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedType,
                onDismissRequest = { vm.dismissTypeDropdown() }
            ) {
                vehicleTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            vm.setSelectedType(type)
                            vm.dismissTypeDropdown()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (plateNumber.isNotBlank() && selectedDriver.isNotBlank() && vm.isValidPlateNumber(plateNumber)) {
                    val updatedDelivery = delivery.copy(
                        plateNumber = plateNumber,
                        driverName = selectedDriver,
                        type = selectedType
                    )
                    onSave(updatedDelivery)
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = plateNumber.isNotBlank() && selectedDriver.isNotBlank() && vm.isValidPlateNumber(plateNumber)
        ) {
            Text("Save")
        }
    }
}
