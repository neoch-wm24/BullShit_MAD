package com.example.delivery_and_transportation_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.Delivery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeliveryScreen(
    delivery: Delivery,
    navController: NavController,
    onSave: (Delivery) -> Unit
) {
    var plateNumber by remember { mutableStateOf(delivery.plateNumber) }
    var driverName by remember { mutableStateOf(delivery.driverName) }
    var type by remember { mutableStateOf(delivery.type) }
    var date by remember { mutableStateOf(delivery.date) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Delivery ") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    )  { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = plateNumber,
                onValueChange = { plateNumber = it },
                label = { Text("Plate Number") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = driverName,
                onValueChange = { driverName = it },
                label = { Text("Driver Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Vehicle Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val updatedDelivery = delivery.copy(
                        plateNumber = plateNumber,
                        driverName = driverName,
                        type = type,
                        date = date
                    )
                    onSave(updatedDelivery)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}
