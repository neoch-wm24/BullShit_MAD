package com.example.order_management.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun EditOrderScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    orderId: String
) {
    val db = FirebaseFirestore.getInstance()

    var order by remember { mutableStateOf<Order?>(null) }
    var parcels by remember { mutableStateOf<List<Parcel>>(emptyList()) }
    var originalParcelIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var showParcelDialog by remember { mutableStateOf(false) }
    var currentParcel by remember { mutableStateOf(Parcel()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        try {
            val snapshot = db.collection("orders").document(orderId).get().await()
            if (snapshot.exists()) {
                val senderId = snapshot.getString("sender_id") ?: ""
                val receiverId = snapshot.getString("receiver_id") ?: ""
                val parcelIds = when (val parcelIdsData = snapshot.get("parcel_ids")) {
                    is List<*> -> parcelIdsData.filterIsInstance<String>()
                    else -> emptyList()
                }
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

                originalParcelIds = parcelIds

                val parcelSnapshots = db.collection("parcels")
                    .whereIn("id", parcelIds.ifEmpty { listOf("dummy") })
                    .get().await()

                parcels = parcelSnapshots.documents.map { doc ->
                    Parcel(
                        id = doc.getString("id") ?: "",
                        description = doc.getString("description") ?: "",
                        weight = doc.getString("weight") ?: "",
                        dimensions = doc.getString("dimensions") ?: ""
                    )
                }
            }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load order: ${e.message}"
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(errorMessage ?: "Error", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    order?.let { currentOrder ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
                .padding(16.dp,16.dp, 16.dp, bottom = 25.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 🔹 Order Info
                item { OrderInfoSection(order = currentOrder) }

                // 🔹 Sender
                item {
                    CustomerSelector(
                        title = "Sender",
                        selectedCustomerId = currentOrder.senderId,
                        onCustomerSelected = { customerId ->
                            order = currentOrder.copy(senderId = customerId)
                        }
                    )
                }

                // Receiver
                item {
                    CustomerSelector(
                        title = "Receiver",
                        selectedCustomerId = currentOrder.receiverId,
                        onCustomerSelected = { customerId ->
                            order = currentOrder.copy(receiverId = customerId)
                        }
                    )
                }

                item {
                    ParcelListSection(
                        parcels = parcels,
                        onAddParcel = { showParcelDialog = true },
                        onDeleteParcel = { index ->
                            val updatedParcels = parcels.toMutableList().apply { removeAt(index) }
                            parcels = updatedParcels
                            order = currentOrder.copy(parcelIds = updatedParcels.map { it.id })

                            val newTotalWeight = updatedParcels.sumOf { it.weight.toDoubleOrNull() ?: 0.0 }
                            val newCost = if (updatedParcels.isNotEmpty()) 5.0 + newTotalWeight * 2 else 0.0
                            order = currentOrder.copy(totalWeight = newTotalWeight, cost = newCost)
                        }
                    )
                }

                item { SummarySection(order = currentOrder) }

                item {
                    Button(
                        onClick = {
                            isUpdating = true

                            updateOrderInFirebase(
                                db = db,
                                orderId = orderId,
                                updatedOrder = currentOrder,
                                updatedParcels = parcels,
                                originalParcelIds = originalParcelIds,
                                onSuccess = {
                                    isUpdating = false
                                    navController.popBackStack()
                                },
                                onError = { error ->
                                    isUpdating = false
                                    errorMessage = "Failed to update order: $error"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating &&
                                currentOrder.senderId.isNotEmpty() &&
                                currentOrder.receiverId.isNotEmpty() &&
                                currentOrder.parcelIds.isNotEmpty()
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Update Order")
                        }
                    }
                }
            }
        }

        // Add Parcel Dialog
        if (showParcelDialog) {
            AddParcelDialog(
                parcel = currentParcel,
                onParcelChange = { currentParcel = it },
                onConfirm = {
                    val sequenceNumber = currentOrder.parcelIds.size + 1
                    val parcelId = generateParcelId(currentOrder.id, sequenceNumber)

                    val newParcel = currentParcel.copy(id = parcelId)
                    val weightNum = currentParcel.weight.toDoubleOrNull() ?: 0.0
                    val newTotalWeight = currentOrder.totalWeight + weightNum
                    val newCost = 5 + newTotalWeight * 2

                    parcels = parcels + newParcel
                    order = currentOrder.copy(
                        parcelIds = currentOrder.parcelIds + parcelId,
                        totalWeight = newTotalWeight,
                        cost = newCost
                    )

                    currentParcel = Parcel()
                    showParcelDialog = false
                },
                onDismiss = {
                    currentParcel = Parcel()
                    showParcelDialog = false
                }
            )
        }
    }
}

private fun updateOrderInFirebase(
    db: FirebaseFirestore,
    orderId: String,
    updatedOrder: Order,
    updatedParcels: List<Parcel>,
    originalParcelIds: List<String>,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val currentParcelIds = updatedParcels.map { it.id }
        val parcelsToDelete = originalParcelIds.filter { it !in currentParcelIds }

        val deletePromises = parcelsToDelete.map { parcelId ->
            db.collection("parcels").document(parcelId).delete()
        }

        val savePromises = updatedParcels.map { parcel ->
            val parcelData = mapOf(
                "id" to parcel.id,
                "description" to parcel.description,
                "weight" to parcel.weight,
                "dimensions" to parcel.dimensions
            )
            db.collection("parcels").document(parcel.id).set(parcelData)
        }

        val allPromises = deletePromises + savePromises

        if (allPromises.isNotEmpty()) {
            allPromises.first()
                .addOnSuccessListener {
                    updateOrderDocument(db, orderId, updatedOrder, currentParcelIds, onSuccess, onError)
                }
                .addOnFailureListener { e ->
                    onError("Failed to update parcels: ${e.message}")
                }
        } else {
            updateOrderDocument(db, orderId, updatedOrder, currentParcelIds, onSuccess, onError)
        }

    } catch (e: Exception) {
        onError("Unexpected error: ${e.message}")
    }
}

private fun updateOrderDocument(
    db: FirebaseFirestore,
    orderId: String,
    updatedOrder: Order,
    currentParcelIds: List<String>,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val orderData = mapOf(
        "sender_id" to updatedOrder.senderId,
        "receiver_id" to updatedOrder.receiverId,
        "parcel_ids" to currentParcelIds,
        "total_weight" to updatedOrder.totalWeight,
        "cost" to updatedOrder.cost
    )

    db.collection("orders").document(orderId).update(orderData)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            onError("Failed to update order: ${e.message}")
        }
}