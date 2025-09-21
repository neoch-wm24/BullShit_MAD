package com.example.order_management.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject

/* ------------------- Order Detail Screen ------------------- */
@Composable
fun OrderDetailScreen(orderId: String, navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()

    var order by remember { mutableStateOf<Order?>(null) }
    var parcels by remember { mutableStateOf<List<Parcel>>(emptyList()) }
    var senderName by remember { mutableStateOf("Loading...") }
    var receiverName by remember { mutableStateOf("Loading...") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Firestore listeners (保持你的逻辑不变)
    DisposableEffect(orderId) {
        var parcelListener: ListenerRegistration? = null
        val orderListener = db.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Failed to get order: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val senderId = snapshot.getString("sender_id") ?: ""
                    val receiverId = snapshot.getString("receiver_id") ?: ""
                    val parcelIds = (snapshot.get("parcel_ids") as? List<*>)
                        ?.filterIsInstance<String>()
                        ?: emptyList()
                    val totalWeight = snapshot.getDouble("total_weight") ?: 0.0
                    val cost = snapshot.getDouble("cost") ?: 0.0

                    order = Order(
                        id = orderId,
                        senderId = senderId,
                        receiverId = receiverId,
                        parcelIds = parcelIds,
                        totalWeight = totalWeight,
                        cost = cost
                    )

                    if (senderId.isNotEmpty()) {
                        db.collection("customers").whereEqualTo("id", senderId).limit(1).get()
                            .addOnSuccessListener { result ->
                                senderName = result.documents.firstOrNull()?.getString("name") ?: senderId
                            }
                    }

                    if (receiverId.isNotEmpty()) {
                        db.collection("customers").whereEqualTo("id", receiverId).limit(1).get()
                            .addOnSuccessListener { result ->
                                receiverName = result.documents.firstOrNull()?.getString("name") ?: receiverId
                            }
                    }

                    parcelListener?.remove()
                    if (parcelIds.isNotEmpty()) {
                        parcelListener = db.collection("parcels")
                            .whereIn(FieldPath.documentId(), parcelIds)
                            .addSnapshotListener { result, err ->
                                if (err != null) {
                                    println("Failed to get parcels: ${err.message}")
                                    return@addSnapshotListener
                                }
                                if (result != null) {
                                    parcels = result.documents.map { doc ->
                                        Parcel(
                                            id = doc.id,
                                            description = doc.getString("description") ?: "",
                                            weight = doc.getString("weight") ?: "",
                                            dimensions = doc.getString("dimensions") ?: ""
                                        )
                                    }
                                }
                            }
                    } else {
                        parcels = emptyList()
                    }
                }
            }
        onDispose {
            orderListener.remove()
            parcelListener?.remove()
        }
    }

    // QR Code
    val qrCodeBitmap by remember(orderId, senderName, receiverName, parcels) {
        mutableStateOf(
            if (senderName != "Loading..." && receiverName != "Loading...") {
                generateQRCode(
                    JSONObject().apply {
                        put("orderId", orderId)
                        put("sender", senderName)
                        put("receiver", receiverName)
                        put("parcelCount", parcels.size)
                    }.toString()
                )
            } else null
        )
    }

    // ------------------- UI -------------------
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp,16.dp, 16.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Order ID: $orderId",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))

            qrCodeBitmap?.let { bmp ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Order QR Code",
                        modifier = Modifier.size(160.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Customer Details", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Sender: $senderName", fontSize = 14.sp)
                    Text("Receiver: $receiverName", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            order?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Order Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Total Weight: ${it.totalWeight} kg", fontSize = 14.sp)
                        Text("Shipping Fee: RM ${"%.2f".format(it.cost)}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Parcel List (${parcels.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        items(parcels.size) { index ->
            val parcel = parcels[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row {
                        Text("${index + 1}. Parcel ID: ", fontWeight = FontWeight.Medium)
                        Text(parcel.id, fontSize = 14.sp)
                    }
                    if (parcel.description.isNotEmpty()) Text("Description: ${parcel.description}", fontSize = 14.sp)
                    if (parcel.weight.isNotEmpty()) Text("Weight: ${parcel.weight} kg", fontSize = 14.sp)
                    if (parcel.dimensions.isNotEmpty()) Text("Dimensions: ${parcel.dimensions}", fontSize = 14.sp)
                }
            }
        }

        // ------------------- Action Buttons -------------------
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("Edit_Order/$orderId") },
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 90.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", maxLines = 1, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 90.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", maxLines = 1, fontSize = 14.sp)
                }
            }
        }
    }

    // ------------------- Delete Confirmation -------------------
    if (showDeleteDialog && order != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Delete", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete Order ${order!!.id}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirebaseFirestore.getInstance()
                            .collection("orders")
                            .document(order!!.id)
                            .delete()
                        showDeleteDialog = false
                        navController.popBackStack()
                    }
                ) { Text("Confirm", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


/* ------------------- QR Code Generator ------------------- */
fun generateQRCode(text: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 300, 300)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = if (bitMatrix[x, y]) Color.Black.toArgb() else Color.White.toArgb()
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}