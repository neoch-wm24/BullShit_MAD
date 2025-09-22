package com.example.warehouse_management.ui.screen

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
                // 保存 rack 信息
                RackManager.setCurrentRack(selectedRack)

                // 保存 parcel 信息
                val senderInfo = AddressInfo(
                    name = sender,
                    phone = "",
                    addressLine = "",
                    city = "",
                    postalCode = "",
                    state = ""
                )

                val receiverInfo = AddressInfo(
                    name = receiver,
                    phone = "",
                    addressLine = "",
                    city = "",
                    postalCode = "",
                    state = ""
                )

                val associatedRackId = RackManager.getRackByName(selectedRack)?.id ?: ""

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
                ParcelDataManager.addOrder(orderData)

                // Navigate to homepage instead of going back
                navController?.navigate("home") {
                    // Clear the back stack to prevent going back to scan screen
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = hasRacks && selectedRack.isNotEmpty()
        ) {
            Text("Instock", color = Color.Black)
        }
    }
}