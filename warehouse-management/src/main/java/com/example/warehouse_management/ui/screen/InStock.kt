package com.example.warehouse_management.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.core_data.AllParcelData
import com.example.core_data.AddressInfo
import com.example.core_data.ParcelInfo
import com.example.core_data.ParcelDataManager
import com.example.core_data.RackManager
import com.example.core_ui.theme.PrimaryColor
import java.util.Date
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InStockScreen(
    orderId: String,
    sender: String,
    receiver: String,
    parcelCount: Int,
    totalWeight: String,
    modifier: Modifier = Modifier,
    navController: NavHostController? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedRack by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val rackNames = RackManager.getRackNames()
    val hasRacks = rackNames.isNotEmpty()

    val displayText = if (hasRacks) {
        if (selectedRack.isEmpty()) "请选择一个货架" else selectedRack
    } else {
        "Please add a Rack first"
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        // ---------- 扫描信息 Card ----------
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("入库单号: $orderId")
                Text("发件人: $sender")
                Text("收件人: $receiver")
                Text("包裹数量: $parcelCount")
                Text("总重量: $totalWeight kg")
            }
        }

        // ---------- Available Rack ----------
        Text(
            text = "Available Rack",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (hasRacks) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                enabled = hasRacks,
                label = { Text("Rack", color = Color.Black) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )

            if (hasRacks) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    rackNames.forEach { rackName ->
                        DropdownMenuItem(
                            text = { Text(rackName, color = PrimaryColor) },
                            onClick = {
                                selectedRack = rackName
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = PrimaryColor,
                                disabledTextColor = PrimaryColor.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---------- Instock 按钮 ----------
        Button(
            onClick = {
                scope.launch {
                    try {
                        // 保存 rack 信息
                        RackManager.setCurrentRack(selectedRack)

                        val senderInfo = AddressInfo(name = sender)
                        val receiverInfo = AddressInfo(name = receiver)

                        // derive rack id by matching name from RackManager.rackList
                        val associatedRackId = RackManager.rackList
                            .firstOrNull { it.name == selectedRack }
                            ?.id ?: ""

                        val orderData = AllParcelData(
                            id = orderId,
                            sender = senderInfo,
                            recipient = receiverInfo,
                            parcels = List(parcelCount) {
                                ParcelInfo(
                                    id = "Parcel ${(it + 1)}",
                                    description = "Package ${(it + 1)}",
                                    weight = totalWeight,
                                    dimensions = "Unknown",
                                    value = "Unknown"
                                )
                            },
                            timestamp = Date(),
                            rackId = associatedRackId
                        )

                        // ✅ 保存订单到 Firestore
                        ParcelDataManager.addOrder(orderData)

                        errorMessage = null
                        navController?.navigate("home") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        Log.e("InStockScreen", "Failed to save order", e)
                        errorMessage = e.message ?: "Failed to In-Stock. Please try again."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = hasRacks && selectedRack.isNotEmpty()
        ) {
            Text("Instock", color = Color.Black)
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(text = errorMessage ?: "", color = Color(0xFFD32F2F))
        }
    }
}