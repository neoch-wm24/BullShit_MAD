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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.user_management.viewmodel.EditUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    navController: NavHostController,
    customerId: String
) {
    // ViewModel holds all UI + form state (replaces remember vars)
    val vm: EditUserViewModel = viewModel()
    val name by vm.name.collectAsState()
    val phone by vm.phone.collectAsState()
    val email by vm.email.collectAsState()
    val address by vm.address.collectAsState()
    val postcode by vm.postcode.collectAsState()
    val city by vm.city.collectAsState()
    val stateText by vm.stateText.collectAsState()
    val isSaving by vm.isSaving.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()
    val showSuccess by vm.showSuccess.collectAsState()

    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load customer data once per id
    LaunchedEffect(customerId) {
        vm.ensureLoad(customerId) { id ->
            val snap = db.collection("customers").document(id).get().await()
            if (!snap.exists()) null else EditUserViewModel.CustomerData(
                name = snap.getString("name") ?: "",
                phone = snap.get("phone")?.toString() ?: "",
                email = snap.getString("email") ?: "",
                address = snap.getString("address") ?: "",
                postcode = snap.get("postcode")?.toString() ?: "",
                city = snap.getString("city") ?: "",
                state = snap.getString("state") ?: ""
            )
        }
    }

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            snackbarHostState.showSnackbar("Customer Updated Successfully")
            vm.consumeSuccess()
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
                .verticalScroll(rememberScrollState()), // Always scrollable portrait & landscape
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
                        onValueChange = { vm.setName(it) },
                        label = { Text("Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { vm.setPhone(it.filter { ch -> ch.isDigit() }) },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { vm.setEmail(it.trim()) },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { vm.setAddress(it) },
                        label = { Text("Address") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = "Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = postcode,
                        onValueChange = { vm.setPostcode(it.filter { ch -> ch.isDigit() }) },
                        label = { Text("Postcode") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Postcode") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { vm.setCity(it.trim()) },
                        label = { Text("City") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = "City") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = stateText,
                        onValueChange = { vm.setStateText(it.trim()) },
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
                    text = errorMessage ?: "",
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
                        if (name.isNotBlank() && phone.isNotBlank() && email.isNotBlank() &&
                            address.isNotBlank() && postcode.isNotBlank() && city.isNotBlank() && stateText.isNotBlank()
                        ) {
                            vm.setSaving(true)
                            vm.setError(null)
                            val updatedCustomer = hashMapOf(
                                "name" to name.trim().uppercase(),
                                "phone" to phone.trim(),
                                "email" to email.trim(),
                                "address" to address.trim(),
                                "postcode" to postcode.trim(),
                                "city" to city.trim(),
                                "state" to stateText.trim()
                            )
                            db.collection("customers")
                                .document(customerId)
                                .update(updatedCustomer as Map<String, Any>)
                                .addOnSuccessListener {
                                    vm.setSaving(false)
                                    vm.triggerSuccess()
                                    scope.launch {
                                        delay(1000)
                                        navController.popBackStack()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    vm.setSaving(false)
                                    vm.setError("Update failed: ${e.message}")
                                }
                        } else {
                            vm.setError("All fields must be filled")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) { Text(if (isSaving) "Saving..." else "Save") }

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }
            }
        }
    }
}
