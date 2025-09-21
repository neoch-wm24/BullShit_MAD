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
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    navController: NavController,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            snackbarHostState.showSnackbar("Customer Saved Successfully")
            showSuccess = false
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
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
                        onValueChange = { phone = it.filter { ch -> ch.isDigit() } }, // ✅ 只允许数字
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
                        onValueChange = { postcode = it.filter { ch -> ch.isDigit() } }, // ✅ 只允许数字
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

            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && email.isNotBlank() &&
                        address.isNotBlank() && postcode.isNotBlank() &&
                        city.isNotBlank() && state.isNotBlank()
                    ) {
                        isSaving = true
                        errorMessage = null

                        val newCustomer = hashMapOf(
                            "id" to UUID.randomUUID().toString(),
                            "name" to name.trim().uppercase(),
                            "phone" to phone.trim(), // 保留为 String
                            "email" to email.trim(),
                            "address" to address.trim(),
                            "postcode" to postcode.trim(), // 保留为 String
                            "city" to city.trim(),
                            "state" to state.trim()
                        )

                        db.collection("customers")
                            .add(newCustomer)
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
                                errorMessage = "Save failed: ${e.message}"
                            }

                    } else {
                        errorMessage = "All fields must be filled"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isSaving,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        }
    }
}
