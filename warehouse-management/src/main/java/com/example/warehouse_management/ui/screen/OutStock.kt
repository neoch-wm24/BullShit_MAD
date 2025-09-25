package com.example.warehouse_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.core_data.AllParcelData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OutStockScreen(
    orderId: String,
    sender: String,
    receiver: String,
    parcelCount: Int,
    rackId: String,
    currentRackName: String,
    modifier: Modifier = Modifier,
    navController: NavHostController? = null
) {
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    // ✅ Add state to retrieve and display full order data
    var orderData by remember { mutableStateOf<AllParcelData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ✅ Fetch full order data from racks collection
    LaunchedEffect(orderId, rackId) {
        try {
            if (orderId.isNotEmpty()) {
                if (rackId == "AUTO_DETECT") {
                    // ✅ Search all racks to find where this order is stored
                    println("OutStock: Auto-detecting rack for order $orderId")
                    val racksSnapshot = db.collection("racks").get().await()

                    for (rackDoc in racksSnapshot.documents) {
                        val currentRackId = rackDoc.id
                        val orderSnapshot = db.collection("racks")
                            .document(currentRackId)
                            .collection("orders")
                            .document(orderId)
                            .get()
                            .await()

                        if (orderSnapshot.exists()) {
                            // Found the order in this rack
                            orderData = orderSnapshot.toObject(AllParcelData::class.java)?.copy(rackId = currentRackId)
                            println("OutStock: Found order $orderId in rack $currentRackId")
                            break
                        }
                    }

                    if (orderData == null) {
                        errorMessage = "Order not found in any rack"
                        println("OutStock: Order $orderId not found in any rack")
                    }
                } else if (rackId.isNotEmpty()) {
                    // ✅ Direct lookup with known rackId
                    val snapshot = db.collection("racks")
                        .document(rackId)
                        .collection("orders")
                        .document(orderId)
                        .get()
                        .await()

                    if (snapshot.exists()) {
                        orderData = snapshot.toObject(AllParcelData::class.java)
                        println("OutStock: Successfully retrieved order data for $orderId from rack $rackId")
                    } else {
                        errorMessage = "Order not found in rack"
                        println("OutStock: Order $orderId not found in rack $rackId")
                    }
                } else {
                    errorMessage = "No rack information provided"
                }
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load order data: ${e.message}"
            println("OutStock: Error loading order - ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        // ✅ Show loading state
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // ✅ Show error message if any
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = "Error: $error",
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // ✅ Display retrieved order data or fallback to navigation parameters
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Order Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF69B4),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                orderData?.let { data ->
                    // ✅ Show complete order data from racks collection
                    Text("Order ID: ${data.id}")
                    Text("Sender: ${data.sender.name}")
                    Text("Sender Phone: ${data.sender.phone}")
                    Text("Sender Address: ${data.sender.addressLine}, ${data.sender.city}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Recipient: ${data.recipient.name}")
                    Text("Recipient Phone: ${data.recipient.phone}")
                    Text("Recipient Address: ${data.recipient.addressLine}, ${data.recipient.city}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Parcel Count: ${data.parcels.size}")
                    Text("Status: ${data.status}")
                    Text("Date: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(data.timestamp)}")
                } ?: run {
                    // ✅ Fallback to navigation parameters if no data retrieved
                    Text("Order ID: $orderId")
                    Text("Sender: $sender")
                    Text("Recipient: $receiver")
                    Text("Parcel Count: $parcelCount")
                    if (!isLoading) {
                        Text("Note: Using QR scan data (order not found in rack)",
                             color = Color(0xFFFF9800),
                             fontSize = 12.sp)
                    }
                }
            }
        }

        Text(
            text = "Current Rack",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                // ✅ Show rack info from retrieved order data or fallback
                orderData?.let { data ->
                    // Get rack name from RackManager using the detected rackId
                    val detectedRackName = com.example.core_data.RackManager.getRackById(data.rackId)?.name
                    Text("Rack: ${detectedRackName ?: data.rackId}")
                    Text("Rack ID: ${data.rackId}")
                } ?: run {
                    Text(if (currentRackName == "AUTO_DETECT") "Auto-detecting rack..." else currentRackName.ifEmpty { "No rack info available" })
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        // ✅ Use the detected rackId from orderData if available
                        val targetRackId = orderData?.rackId ?: rackId

                        if (targetRackId.isNotEmpty() && targetRackId != "AUTO_DETECT") {
                            // Delete the order from the detected rack
                            db.collection("racks")
                                .document(targetRackId)
                                .collection("orders")
                                .document(orderId)
                                .delete()
                                .await()

                            println("OutStock: Successfully deleted order $orderId from rack $targetRackId")

                            navController?.navigate("home") {
                                popUpTo("home") { inclusive = false }
                            }
                        } else {
                            println("OutStock: Cannot delete - no valid rack ID found")
                        }
                    } catch (e: Exception) {
                        println("OutStock update failed: ${e.message}")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = (orderData?.rackId?.isNotEmpty() == true && orderData?.rackId != "AUTO_DETECT") || (rackId.isNotEmpty() && rackId != "AUTO_DETECT"),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Outstock", color = Color.Black)
        }
    }
}