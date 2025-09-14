package com.example.user_management.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    navController: NavHostController,
    customerId: String
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load customer data
    LaunchedEffect(customerId) {
        try {
            val snapshot = db.collection("customers").document(customerId).get().await()
            if (snapshot.exists()) {
                name = snapshot.getString("name") ?: ""
                phone = snapshot.get("phone")?.toString() ?: ""
                email = snapshot.getString("email") ?: ""
                address = snapshot.getString("address") ?: ""
                postcode = snapshot.get("postcode")?.toString() ?: ""
                city = snapshot.getString("city") ?: ""
                state = snapshot.getString("state") ?: ""
            } else {
                errorMessage = "Customer does not exist"
            }
        } catch (e: Exception) {
            errorMessage = "Loading failed: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            snackbarHostState.showSnackbar("Customer Updated Successfully")
            showSuccess = false
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = "Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = postcode,
                        onValueChange = { postcode = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Postcode") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Postcode") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it.trim() },
                        label = { Text("City") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = "City") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it.trim() },
                        label = { Text("State") },
                        leadingIcon = { Icon(Icons.Default.Map, contentDescription = "State") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        // Input validation
                        if (name.isNotBlank() && phone.isNotBlank() && email.isNotBlank() &&
                            address.isNotBlank() && postcode.isNotBlank() &&
                            city.isNotBlank() && state.isNotBlank()
                        ) {
                            isSaving = true
                            errorMessage = null

                            val updatedCustomer = hashMapOf(
                                "name" to name.trim().uppercase(),
                                "phone" to phone.trim(), // 保留为 String
                                "email" to email.trim(),
                                "address" to address.trim(),
                                "postcode" to postcode.trim(), // 保留为 String
                                "city" to city.trim(),
                                "state" to state.trim()
                            )


                            db.collection("customers")
                                .document(customerId)
                                .update(updatedCustomer as Map<String, Any>)
                                .addOnSuccessListener {
                                    isSaving = false
                                    showSuccess = true
                                    scope.launch {
                                        delay(1000)
                                        navController.popBackStack()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    errorMessage = "Update failed: ${e.message}"
                                }
                        } else {
                            errorMessage = "All fields must be filled"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    Text(if (isSaving) "Saving..." else "Save")
                }

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
