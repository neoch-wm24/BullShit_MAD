package com.example.order_and_parcel_management.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.core_data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrderandParcelScreen(navController: NavHostController) {
    var sender by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    val parcels = remember { mutableStateListOf("") }
    val context = LocalContext.current

    // Rak 下拉菜单数据
    var selectedRakId by remember { mutableStateOf<String?>(null) }
    var selectedRakLabel by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    val rakOptions: List<Pair<String, String>> = remember {
        RakManager.rakList.map { rakInfo ->
            rakInfo.id to "${rakInfo.name} (${rakInfo.layer})"
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Rak",
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (rakOptions.isEmpty()) {
                    Text(
                        text = "No Rak available. Please add one before creating orders.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = isExpanded,
                        onExpandedChange = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            readOnly = true,
                            value = selectedRakLabel,
                            onValueChange = { },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            placeholder = { Text("Select Rak") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rakOptions.forEach { (rakId, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedRakId = rakId
                                        selectedRakLabel = label
                                        isExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { parcels.add("") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Parcel")
                }

                Button(
                    onClick = {
                        if (sender.isBlank() || recipient.isBlank() || parcels.any { it.isBlank() } || selectedRakId.isNullOrBlank()) {
                            Toast.makeText(context, "Please fill in all fields and select a Rak", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val senderInfo = SenderInfo(sender)
                        val recipientInfo = RecipientInfo(recipient)
                        val parcelInfoList = parcels.map { parcel -> ParcelInfo(information = parcel) }

                        // Create and store the order
                        val order = AllParcelData(
                            sender = senderInfo,
                            recipient = recipientInfo,
                            parcels = parcelInfoList,
                            rakId = selectedRakId // Associate order with selected Rak
                        )

                        ParcelDataStore.addOrder(order)
                        Toast.makeText(context, "Order saved successfully", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm")
                }

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sender Information", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    OutlinedTextField(
                        value = sender,
                        onValueChange = { sender = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("Type your text here", fontSize = 12.sp) }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Recipient Information", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("Type your text here", fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(modifier = Modifier.weight(1f, fill = true)) {
                itemsIndexed(parcels) { index, parcel ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Parcel Information ${index + 1}")
                        OutlinedTextField(
                            value = parcel,
                            onValueChange = { newValue -> parcels[index] = newValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Type your parcel detail here", fontSize = 12.sp) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true, showSystemUi = true)
fun AddOrderandParcelScreenPreview() {
    val navController = rememberNavController()
    AddOrderandParcelScreen(navController = navController)
}
