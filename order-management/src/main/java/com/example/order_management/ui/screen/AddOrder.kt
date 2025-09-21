package com.example.order_management.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.order_management.ui.screen.OrderIdGenerator.generateOrderId
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.hashids.Hashids
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger


/* ----------------------------------------- Data Class ----------------------------------------- */
data class Parcel(
    val id: String = "",
    val description: String = "",
    val weight: String = "",
    val dimensions: String = "",
)

data class Order(
    val id: String = generateOrderId(),
    val senderId: String = "",
    val receiverId: String = "",
    val parcelIds: List<String> = emptyList(),
    val totalWeight: Double = 0.0,
    val cost: Double = 0.0
)

/* ----------------------------------- Save Data To Firestore ----------------------------------- */
fun saveParcelToFirestore(parcel: Parcel, onComplete: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val data = mapOf(
        "id" to parcel.id,
        "description" to parcel.description,
        "weight" to parcel.weight,
        "dimensions" to parcel.dimensions
    )
    db.collection("parcels").document(parcel.id).set(data)
        .addOnSuccessListener { onComplete(parcel.id) }
}

fun saveOrderToFirestore(order: Order) {
    val db = FirebaseFirestore.getInstance()
    val data = mapOf(
        "id" to order.id,
        "sender_id" to order.senderId,
        "receiver_id" to order.receiverId,
        "parcel_ids" to order.parcelIds,
        "total_weight" to order.totalWeight,
        "cost" to order.cost
    )
    db.collection("orders").document(order.id).set(data)
}

/* --------------------------------------------- UI --------------------------------------------- */
@Composable
fun AddOrderScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var order by remember { mutableStateOf(Order()) }
    var currentParcel by remember { mutableStateOf(Parcel()) }
    var showParcelDialog by remember { mutableStateOf(false) }
    var parcels by remember { mutableStateOf<List<Parcel>>(emptyList()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 订单ID + QR
            item { OrderInfoSection(order = order) }

            // Sender Selector
            item {
                CustomerSelector(
                    title = "Sender",
                    onCustomerSelected = { customerId -> order = order.copy(senderId = customerId) }
                )
            }

            // Receiver Selector
            item {
                CustomerSelector(
                    title = "Receiver",
                    onCustomerSelected = { customerId -> order = order.copy(receiverId = customerId) }
                )
            }

            // Parcel
            item {
                ParcelListSection(
                    parcels = parcels,
                    onAddParcel = { showParcelDialog = true },
                    onDeleteParcel = { index ->
                        // 删除包裹
                        parcels = parcels.toMutableList().apply { removeAt(index) }
                        order = order.copy(parcelIds = order.parcelIds.toMutableList().apply { removeAt(index) })

                        val newTotalWeight = parcels.sumOf { it.weight.toDoubleOrNull() ?: 0.0 }
                        val newCost = if (parcels.isNotEmpty()) {
                            5.0 + newTotalWeight * 2
                        } else {
                            0.0
                        }
                        order = order.copy(totalWeight = newTotalWeight, cost = newCost)
                    }
                )
            }

            // Order Summary
            item { SummarySection(order = order) }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        parcels.forEach { parcel ->
                            saveParcelToFirestore(parcel) { }
                        }
                        saveOrderToFirestore(order)
                        navController.navigate("order") {
                            popUpTo("AddOrder") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = order.senderId.isNotEmpty() &&
                            order.receiverId.isNotEmpty() &&
                            order.parcelIds.isNotEmpty()
                ) {
                    Text("Submit Order", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Parcel Dialog
    if (showParcelDialog) {
        AddParcelDialog(
            parcel = currentParcel,
            onParcelChange = { currentParcel = it },
            onConfirm = {
                val sequenceNumber = order.parcelIds.size + 1
                val parcelId = generateParcelId(order.id, sequenceNumber)

                val newParcel = currentParcel.copy(id = parcelId)
                val weightNum = currentParcel.weight.toDoubleOrNull() ?: 0.0
                val newTotalWeight = order.totalWeight + weightNum
                val newCost = 5 + newTotalWeight * 2  // Shipping Fee Formula

                parcels = parcels + newParcel
                order = order.copy(
                    parcelIds = order.parcelIds + parcelId,
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

/* -------------------- Order Details -------------------- */
@Composable
fun OrderInfoSection(order: Order) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Order ID: ${order.id}", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CustomerSelector(
    title: String,
    onCustomerSelected: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var customers by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }

    // Load customers from Firestore
    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("customers").get().await()
            customers = snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: "unknown user"
                val address = doc.getString("address") ?: "no address"
                Triple(id, name, address)
            }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "loading customer list failed: ${e.message}"
            isLoading = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // search bar & dropdown button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = selectedCustomer?.second ?: searchText,
                    onValueChange = {
                        if (selectedCustomer == null) {
                            searchText = it
                            expanded = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(
                            when {
                                isLoading -> "loading..."
                                errorMessage != null -> "loading failed"
                                customers.isEmpty() -> "no customer data yet"
                                else -> "Customer Name"
                            }
                        )
                    },
                    enabled = !isLoading && customers.isNotEmpty() && errorMessage == null,
                    singleLine = true,
                    trailingIcon = {
                        Row {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            if (selectedCustomer != null) {
                                IconButton(
                                    onClick = {
                                        selectedCustomer = null
                                        searchText = ""
                                        expanded = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "clear selection",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // dropdown button
                IconButton(
                    onClick = {
                        println("click drop dwn button, customers: ${customers.size}, expanded: $expanded") // test
                        if (!isLoading && customers.isNotEmpty()) {
                            expanded = !expanded
                        }
                    },
                    enabled = !isLoading && customers.isNotEmpty() && errorMessage == null
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "collapse" else "expand"
                    )
                }
            }

            // Customer List
            AnimatedVisibility(
                visible = expanded && customers.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp) // Limit maximum height
                    ) {
                        // filter customers list by search text
                        val filteredCustomers = customers.filter { customer ->
                            customer.second.contains(searchText, ignoreCase = true) ||
                                    customer.first.contains(searchText, ignoreCase = true) ||
                                    customer.third.contains(searchText, ignoreCase = true)
                        }

                        if (filteredCustomers.isEmpty() && searchText.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No matching customers found",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(filteredCustomers.size) { index ->
                                val customer = filteredCustomers[index]

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCustomer = customer
                                            searchText = ""
                                            expanded = false
                                            onCustomerSelected(customer.first) // 传 id
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = customer.second, // name
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Address: ${customer.third}", // address
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (index < filteredCustomers.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // error message display
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ParcelListSection(
    parcels: List<Parcel>,
    onAddParcel: () -> Unit,
    onDeleteParcel: (Int) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Parcel (${parcels.size})", fontWeight = FontWeight.Bold)
                FloatingActionButton(onClick = onAddParcel, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add Parcel")
                }
            }

            if (parcels.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "No parcel yet. Please add on.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                parcels.forEachIndexed { index, parcel ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth()
                        ) {
                            // 🟢 上半部分：Parcel 信息
                            Text("Parcel ID: ${parcel.id}", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            if (parcel.description.isNotEmpty()) {
                                Text("Description: ${parcel.description}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (parcel.weight.isNotEmpty()) {
                                Text("Weight: ${parcel.weight} kg", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (parcel.dimensions.isNotEmpty()) {
                                Text("Dimensions: ${parcel.dimensions}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // 🔽 下半部分：右下角 Delete 按钮
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onDeleteParcel(index) }) {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun AddParcelDialog(
    parcel: Parcel,
    onParcelChange: (Parcel) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Parcel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = parcel.description,
                    onValueChange = { onParcelChange(parcel.copy(description = it)) },
                    label = { Text("Parcel's Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    maxLines = 1
                )
                OutlinedTextField(
                    value = parcel.weight,
                    onValueChange = { onParcelChange(parcel.copy(weight = it)) },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    maxLines = 1
                )
                OutlinedTextField(
                    value = parcel.dimensions,
                    onValueChange = { onParcelChange(parcel.copy(dimensions = it)) },
                    label = { Text("Dimensions") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    maxLines = 1
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = parcel.description.isNotEmpty()) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/* ------------------------------------------- Summary ------------------------------------------ */
@Composable
fun SummarySection(order: Order) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Total Weight: ${order.totalWeight} kg")
            Text("Shipping Fee: RM ${"%.2f".format(Locale.ROOT, order.cost)}")
        }
    }
}

/* ---------------------------------------- ID  Generator --------------------------------------- */
object OrderIdGenerator {
    private val hashids = Hashids("Log-Express", 8, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")
    private val dateFormat = SimpleDateFormat("yyMMdd", Locale.ROOT)
    private val counter = AtomicInteger(0)
    @Volatile private var currentDate: String = dateFormat.format(Date())

    fun generateOrderId(): String {
        val today = dateFormat.format(Date())

        // every new day, reset counter
        if (today != currentDate) {
            synchronized(this) {
                if (today != currentDate) {
                    currentDate = today
                    counter.set(0)
                }
            }
        }

        // Incrementing sequence number
        val sequence = counter.getAndIncrement()

        // Encoding Sequence Numbers with Hashids
        val encoded = hashids.encode(sequence.toLong())

        return "ORD-$today$encoded"
    }
}

fun generateParcelId(orderId: String, sequenceNumber: Int): String {
    val formattedSequence = String.format(Locale.ROOT, "%02d", sequenceNumber)
    return "PAR-${orderId.removePrefix("ORD-")}-$formattedSequence"
}