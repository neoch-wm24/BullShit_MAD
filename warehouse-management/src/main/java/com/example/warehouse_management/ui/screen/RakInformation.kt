package com.example.warehouse_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.core_data.*
import java.util.Locale

@Composable
fun RakInformationScreen(
    navController: NavController,
    rakId: String,
    modifier: Modifier = Modifier
) {
    // ① 判断是否有 Rak 数据
    if (RakManager.rakList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No Rak data available.", color = Color.Gray, fontSize = 18.sp)
        }
        return
    }

    // ② 空 rakId 直接返回上页
    if (rakId.isBlank()) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    // ③ 查找 Rak 数据
    val rakInfo = RakManager.getRakById(rakId)
    if (rakInfo == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Rak not found.", color = Color.Gray, fontSize = 18.sp)
        }
        return
    }

    // ④ 正常显示 - 使用 LazyColumn 包装整个页面
    val allParcelData = ParcelDataStore.getOrdersForRak(rakId) // Only get parcels for this specific Rak

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Rak Information Title (moved out of card)
        item {
            Text(
                text = "Rak Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD05667),
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
            )
        }

        // Rak Information Card (without title)
        item {
            RakInfoCard(rakInfo = rakInfo)
        }

        // Parcel List Title
        item {
            Text(
                text = "Parcel List",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD05667),
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
            )
        }

        // Individual Parcel Cards
        if (allParcelData.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "No parcels available",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(allParcelData) { orderData ->
                ParcelOrderCard(orderData = orderData)
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun RakInfoCard(rakInfo: RakInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            InfoRow("Rak Name:", rakInfo.name)
            DividerSpacer()
            InfoRow("Number of Layers:", "${rakInfo.layer}")
            DividerSpacer()
            InfoRow("Rak State:", rakInfo.state)
        }
    }
}

@Composable
private fun ParcelOrderCard(orderData: AllParcelData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Order ID
            Text(
                text = "Order ID: ${orderData.id}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Date
            Text(
                text = "Date: ${
                    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(orderData.timestamp)
                }",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Horizontal divider
            HorizontalDivider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Sender and Recipient
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sender:", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = orderData.sender.information,
                        fontSize = 14.sp,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Recipient:", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = orderData.recipient.information,
                        fontSize = 14.sp,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Horizontal divider
            HorizontalDivider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Number of Parcels
            Text(
                text = "Number of Parcels: ${orderData.parcels.size}",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )

            // Parcel list
            if (orderData.parcels.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    orderData.parcels.forEachIndexed { index, parcel ->
                        ParcelInfoItem(index = index + 1, parcel = parcel)
                    }
                }
            }

            // Final horizontal divider
            HorizontalDivider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.width(140.dp)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun DividerSpacer() {
    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun ParcelInfoItem(index: Int, parcel: ParcelInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index. ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Text(
            text = parcel.information,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RakInformationScreenPreview() {
    val navController = rememberNavController()
    RakInformationScreen(navController = navController, rakId = "SampleRakId")
}
