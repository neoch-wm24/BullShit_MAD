package com.example.delivery_and_transportation_management.ui.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.delivery_and_transportation_management.data.Stop
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings

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
            if (stops.isNotEmpty()) 12f else 10f
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
            if (stops.isEmpty()) {
                // Show message when no stops available
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No delivery stops for today",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // ✅ Map at the top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.matchParentSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            mapType = MapType.NORMAL,
                            isTrafficEnabled = false,
                            isMyLocationEnabled = false
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            compassEnabled = true,
                            myLocationButtonEnabled = false,
                            rotationGesturesEnabled = true,
                            scrollGesturesEnabled = true,
                            zoomGesturesEnabled = true,
                            tiltGesturesEnabled = true
                        )
                    ) {
                        // Markers for stops
                        stops.forEachIndexed { index, stop ->
                            Marker(
                                state = MarkerState(position = stop.location),
                                title = "Stop ${index + 1}: ${stop.name}",
                                snippet = stop.address
                            )
                        }

                        // Draw polyline if >1 stop
                        if (stops.size > 1) {
                            Polyline(
                                points = stops.map { it.location },
                                color = Color.Blue,
                                width = 5f
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Stops list
            if (stops.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No delivery stops scheduled",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Check back later or contact dispatch",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
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
                                Text("Stop ${index + 1}", style = MaterialTheme.typography.labelSmall)
                                Text(stop.name, style = MaterialTheme.typography.titleMedium)
                                Text(stop.address, style = MaterialTheme.typography.bodySmall)

                                Spacer(modifier = Modifier.height(8.dp))

                                // Action buttons row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Copy address button
                                    val clipboardManager = LocalClipboardManager.current
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                try {
                                                    clipboardManager.setText(AnnotatedString(stop.address))
                                                } catch (e: Exception) {
                                                    // Handle clipboard error silently
                                                }
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Address",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy", style = MaterialTheme.typography.bodySmall)
                                    }

                                    // Open in maps button
                                    val context = LocalContext.current
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                try {
                                                    val gmmIntentUri = Uri.parse(
                                                        "geo:0,0?q=${stop.location.latitude},${stop.location.longitude}(${Uri.encode(stop.name)})"
                                                    )
                                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                                    mapIntent.setPackage("com.google.android.apps.maps")

                                                    // Fallback if Google Maps not installed
                                                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                                                        context.startActivity(mapIntent)
                                                    } else {
                                                        // Open in browser as fallback
                                                        val browserIntent = Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse("https://maps.google.com/?q=${stop.location.latitude},${stop.location.longitude}")
                                                        )
                                                        context.startActivity(browserIntent)
                                                    }
                                                } catch (e: Exception) {
                                                    // Handle intent error silently
                                                }
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Open in Maps",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Navigate", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
