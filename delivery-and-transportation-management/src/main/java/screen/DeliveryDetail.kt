package screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import data.Delivery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetail(
    delivery: Delivery,
    navController: NavController,
    onEdit: (Delivery) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Details") },
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Plate Number: ${delivery.plateNumber}", style = MaterialTheme.typography.bodyLarge)
            Text("Driver Name: ${delivery.driverName}", style = MaterialTheme.typography.bodyLarge)
            Text("Type: ${delivery.type}", style = MaterialTheme.typography.bodyLarge)
            Text("Date: ${delivery.date}", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    onEdit(delivery)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Delivery")
            }
        }
    }
}