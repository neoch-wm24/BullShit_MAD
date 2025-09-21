package com.example.user_management.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun CustomerDetailScreen(
    navController: NavHostController,
    customerId: String
) {
    var customer by remember { mutableStateOf<Customer?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(customerId) {
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("customers").document(customerId).get().await()
            if (snapshot.exists()) {
                customer = Customer(
                    id = snapshot.id,
                    name = snapshot.getString("name") ?: "",
                    phone = snapshot.get("phone")?.toString() ?: "",
                    email = snapshot.getString("email") ?: "",
                    address = snapshot.getString("address") ?: "",
                    postcode = snapshot.get("postcode")?.toString() ?: "",
                    city = snapshot.getString("city") ?: "",
                    state = snapshot.getString("state") ?: ""
                )
            } else {
                errorMessage = "Customer does not exist"
            }
        } catch (e: Exception) {
            errorMessage = "Loading failed: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator()
            errorMessage != null -> Text(
                errorMessage ?: "Loading failed",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            customer != null -> CustomerDetailContent(
                customer = customer!!,
                navController = navController,
                onDeleteClick = { showDeleteDialog = true }
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && customer != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Delete", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${customer!!.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirebaseFirestore.getInstance()
                            .collection("customers")
                            .document(customer!!.id)
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

@Composable
fun CustomerDetailContent(
    customer: Customer,
    navController: NavHostController,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // 使用 verticalScroll 包裹客户信息
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "Customer Details",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(color = Color.LightGray, thickness = 1.dp)

                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp) // 可根据需要限制高度
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoRow(label = "Name", value = customer.name)
                    InfoRow(label = "Phone", value = customer.phone)
                    InfoRow(label = "Email", value = customer.email)
                    InfoRow(label = "Address", value = customer.address)
                    InfoRow(label = "Postcode", value = customer.postcode)
                    InfoRow(label = "City", value = customer.city)
                    InfoRow(label = "State", value = customer.state)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons (保持固定)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("Edit_User/${customer.id}") },
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 90.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", maxLines = 1, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 90.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", maxLines = 1, fontSize = 14.sp)
                }
            }
        }
    }
}


@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Label 固定宽度，保证冒号对齐
        Text(
            text = "$label:",
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier.width(90.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Value 可以换行
        Text(
            text = value,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

