package screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.Delivery

@Composable
fun DeliveryScheduleScreen(
    deliveries: List<Delivery>
) {
    if (deliveries.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No schedules available.")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            deliveries
                .groupBy { it.date }
                .forEach { (date, tasks) ->
                    item {
                        Text(
                            text = "📅 $date",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(tasks) { delivery ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Plate: ${delivery.plateNumber}")
                                Text("Driver: ${delivery.driverName}")
                                Text("Type: ${delivery.type}")
                            }
                        }
                    }
                    item {
                        Divider(Modifier.padding(vertical = 8.dp))
                    }
                }
        }
    }
}

