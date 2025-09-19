package com.example.delivery_and_transportation_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.delivery_and_transportation_management.data.Delivery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetail(
    delivery: Delivery,
    navController: NavController,
    onEdit: (Delivery) -> Unit,
    onDelete: (Delivery) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transportation detail") },
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Plate Number: ${delivery.plateNumber}", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("Driver: ${delivery.driverName}")
                Text("Type: ${delivery.type}")
                Text("Date: ${delivery.date}")
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { onEdit(delivery) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Edit")
                }

                OutlinedButton(
                    onClick = {
                        onDelete(delivery)
                        navController.popBackStack() // back to list after delete
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
