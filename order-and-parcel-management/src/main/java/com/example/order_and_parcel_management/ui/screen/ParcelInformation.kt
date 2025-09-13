package com.example.order_and_parcel_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core_data.*
import java.util.Locale

@Composable
fun ParcelListSection(
    allParcelData: List<AllParcelData>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Parcel List Title
        Text(
            text = "Parcel List",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD05667),
            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
        )

        // Parcel Cards
        if (allParcelData.isEmpty()) {
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
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                allParcelData.forEach { orderData ->
                    ParcelOrderCard(orderData = orderData)
                }
            }
        }
    }
}

fun LazyListScope.parcelListItems(allParcelData: List<AllParcelData>) {
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
}

@Composable
fun ParcelOrderCard(orderData: AllParcelData) {
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
private fun ParcelInfoItem(index: Int, parcel: ParcelInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
