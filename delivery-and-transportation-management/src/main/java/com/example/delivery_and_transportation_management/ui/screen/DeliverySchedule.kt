package com.example.delivery_and_transportation_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.Delivery
import com.example.delivery_and_transportation_management.data.DeliveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScheduleScreen(
    deliveries: List<Delivery>,
    deliveryViewModel: DeliveryViewModel,
    navController: NavController
) {
    var selectedDate by remember { mutableStateOf("") }
    var selectedTransportations by remember { mutableStateOf(setOf<String>()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Date input (text for now, can swap with DatePickerDialog later)
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { selectedDate = it },
            label = { Text("Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Transportation", style = MaterialTheme.typography.titleMedium)

        if (deliveries.isEmpty()) {
            Text("⚠️ No transportation added yet.")
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(deliveries) { delivery ->
                    val plate = delivery.plateNumber.orEmpty().ifBlank { "(No Plate)" }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Checkbox(
                                checked = selectedTransportations.contains(delivery.id),
                                onCheckedChange = { checked ->
                                    selectedTransportations = if (checked)
                                        selectedTransportations + delivery.id
                                    else
                                        selectedTransportations - delivery.id
                                }
                            )
                            Column {
                                Text("$plate - ${delivery.type}")
                                if (!delivery.date.isNullOrBlank()) {
                                    Text("\uD83D\uDCC5 ${delivery.date}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedDate.isNotBlank()) {
                    selectedTransportations.forEach { deliveryId ->
                        deliveryViewModel.updateDeliveryDate(deliveryId, selectedDate)
                    }
                    navController.popBackStack() // ✅ Go back after saving
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedDate.isNotBlank() && selectedTransportations.isNotEmpty()
        ) {
            Text("Save Schedule")
        }
    }
}
