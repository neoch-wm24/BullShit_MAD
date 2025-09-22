package com.example.delivery_and_transportation_management.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.delivery_and_transportation_management.data.Stop
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDeliveryListScreen(
    stops: List<Stop>,
    onCheckout: () -> Unit
) {
    val singapore = com.google.android.gms.maps.model.LatLng(1.3521, 103.8198) // default center
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            stops.firstOrNull()?.location ?: singapore,
            12f
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Deliveries") }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Check Out") },
                icon = { Icon(Icons.Default.Check, contentDescription = "Checkout") },
                onClick = onCheckout
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ✅ Map at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.matchParentSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    // Markers for stops
                    stops.forEach { stop ->
                        Marker(
                            state = MarkerState(position = stop.location),
                            title = stop.name,
                            snippet = stop.address
                        )
                    }

                    // Draw polyline if >1 stop
                    if (stops.size > 1) {
                        Polyline(
                            points = stops.map { it.location },
                            color = Color.Blue,
                            width = 8f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Stops list
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(count = stops.size) { index ->
                    val stop = stops[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(stop.name, style = MaterialTheme.typography.titleMedium)
                            Text(stop.address, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
