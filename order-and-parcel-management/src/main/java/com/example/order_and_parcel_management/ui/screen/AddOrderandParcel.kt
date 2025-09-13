package com.example.order_and_parcel_management.ui.screen

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter
import org.hashids.Hashids
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import com.example.core_data.ParcelDataManager


// 数据类定义
data class Address(
    val name: String = "",
    val phone: String = "",
    val addressLine: String = "",
    val city: String = "",
    val postalCode: String = "",
    val state: String = ""
)

data class Parcel(
    val id: String = generateParcelId(),
    val description: String = "",
    val weight: String = "",
    val dimensions: String = "",
    val value: String = ""
)

data class Order(
    val id: String = generateOrderId(),
    val senderAddress: Address = Address(),
    val receiverAddress: Address = Address(),
    val parcels: List<Parcel> = emptyList()
)

private val hashids = Hashids("Log-Express", 6, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")
private fun getDateFormat() = SimpleDateFormat("yyMMdd", Locale.getDefault())
private val counter = AtomicInteger(0)

fun generateOrderId(): String {
    val dateStr = getDateFormat().format(Date())

    // 秒级时间戳
    val seconds = System.currentTimeMillis() / 1000

    // 序列号，避免同一秒冲突
    val sequence = counter.getAndIncrement() % 1000

    // Hashids 编码
    val encoded = hashids.encode(seconds, sequence.toLong())

    return "ORD-$dateStr$encoded"
}

fun generateParcelId(): String = "PCL${System.currentTimeMillis().toString().takeLast(8)}${(100..999).random()}"

@Composable
fun AddOrderandParcelScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var order by remember { mutableStateOf(Order()) }
    var currentParcel by remember { mutableStateOf(Parcel()) }
    var showParcelDialog by remember { mutableStateOf(false) }

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

            // Order ID 和 QR Code 区域
            item {
                OrderInfoSection(order = order)
            }

            // 寄件者地址
            item {
                AddressSection(
                    title = "寄件者信息",
                    address = order.senderAddress,
                    onAddressChange = { address ->
                        order = order.copy(senderAddress = address)
                    }
                )
            }

            // 收件者地址
            item {
                AddressSection(
                    title = "收件者信息",
                    address = order.receiverAddress,
                    onAddressChange = { address ->
                        order = order.copy(receiverAddress = address)
                    }
                )
            }

            // 包裹列表
            item {
                ParcelListSection(
                    parcels = order.parcels,
                    onAddParcel = { showParcelDialog = true },
                    onDeleteParcel = { index ->
                        order = order.copy(parcels = order.parcels.toMutableList().apply { removeAt(index) })
                    }
                )
            }

            // 提交按钮
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // 处理订单提交逻辑 - 保存数据并导航到搜索页面
                        println("订单提交: $order")

                        // 转换包裹数据到 ParcelInfo 格式
                        val parcelInfoList = order.parcels.map { parcel ->
                            com.example.core_data.ParcelInfo(
                                id = parcel.id,
                                description = parcel.description,
                                weight = parcel.weight,
                                dimensions = parcel.dimensions,
                                value = parcel.value
                            )
                        }

                        // 保存数据到 ParcelDataManager
                        ParcelDataManager.saveOrderFromUI(
                            orderId = order.id,
                            senderName = order.senderAddress.name,
                            senderPhone = order.senderAddress.phone,
                            senderAddressLine = order.senderAddress.addressLine,
                            senderCity = order.senderAddress.city,
                            senderPostalCode = order.senderAddress.postalCode,
                            senderState = order.senderAddress.state,
                            receiverName = order.receiverAddress.name,
                            receiverPhone = order.receiverAddress.phone,
                            receiverAddressLine = order.receiverAddress.addressLine,
                            receiverCity = order.receiverAddress.city,
                            receiverPostalCode = order.receiverAddress.postalCode,
                            receiverState = order.receiverAddress.state,
                            parcels = parcelInfoList
                        )

                        // 导航回搜索页面
                        navController.navigate("search") {
                            popUpTo("add") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = order.senderAddress.name.isNotEmpty() &&
                            order.receiverAddress.name.isNotEmpty() &&
                            order.parcels.isNotEmpty()
                ) {
                    Text("提交订单", fontSize = 16.sp)
                }
            }
        }
    }

    // 添加包裹对话框
    if (showParcelDialog) {
        AddParcelDialog(
            parcel = currentParcel,
            onParcelChange = { currentParcel = it },
            onConfirm = {
                order = order.copy(parcels = order.parcels + currentParcel)
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

@Composable
fun OrderInfoSection(order: Order) {
    var showQRCode by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Order ID: ${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "Show QR Code",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { showQRCode = !showQRCode },
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            AnimatedVisibility(visible = showQRCode) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val qrBitmap = generateQRCode(order.id)
                    qrBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Order QR Code",
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AddressSection(
    title: String,
    address: Address,
    onAddressChange: (Address) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = address.name,
                onValueChange = { onAddressChange(address.copy(name = it)) },
                label = { Text("姓名") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = address.phone,
                onValueChange = { onAddressChange(address.copy(phone = it)) },
                label = { Text("电话号码") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = address.addressLine,
                onValueChange = { onAddressChange(address.copy(addressLine = it)) },
                label = { Text("地址第一行") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = address.city,
                    onValueChange = { onAddressChange(address.copy(city = it)) },
                    label = { Text("城市") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = address.postalCode,
                    onValueChange = { onAddressChange(address.copy(postalCode = it)) },
                    label = { Text("邮政编码") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = address.state,
                onValueChange = { onAddressChange(address.copy(state = it)) },
                label = { Text("州/省") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ParcelListSection(
    parcels: List<Parcel>,
    onAddParcel: () -> Unit,
    onDeleteParcel: (Int) -> Unit
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
                Text(
                    text = "包裹信息 (${parcels.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                FloatingActionButton(
                    onClick = onAddParcel,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加包裹")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            parcels.forEachIndexed { index, parcel ->
                ParcelItem(
                    parcel = parcel,
                    onDelete = { onDeleteParcel(index) }
                )
                if (index < parcels.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (parcels.isEmpty()) {
                Text(
                    text = "暂无包裹，请点击右上角按钮添加",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ParcelItem(
    parcel: Parcel,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "包裹编号: ${parcel.id}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除包裹",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 包裹条形码
            val barcodeBitmap = generateBarcode(parcel.id)
            barcodeBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Parcel Barcode",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(4.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("描述: ${parcel.description.ifEmpty { "未填写" }}")
            Text("重量: ${parcel.weight.ifEmpty { "未填写" }}")
            Text("尺寸: ${parcel.dimensions.ifEmpty { "未填写" }}")
            Text("价值: ${parcel.value.ifEmpty { "未填写" }}")
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
        title = { Text("添加包裹") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "包裹编号: ${parcel.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = parcel.description,
                    onValueChange = { onParcelChange(parcel.copy(description = it)) },
                    label = { Text("包裹描述") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = parcel.weight,
                    onValueChange = { onParcelChange(parcel.copy(weight = it)) },
                    label = { Text("重量 (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = parcel.dimensions,
                    onValueChange = { onParcelChange(parcel.copy(dimensions = it)) },
                    label = { Text("尺寸 (长x宽x高 cm)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = parcel.value,
                    onValueChange = { onParcelChange(parcel.copy(value = it)) },
                    label = { Text("价值 (RM)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = parcel.description.isNotEmpty()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// QR Code 生成函数
fun generateQRCode(text: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 300, 300)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = if (bitMatrix[x, y]) Color.Black.toArgb() else Color.White.toArgb()
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// 条形码生成函数
fun generateBarcode(text: String): Bitmap? {
    return try {
        val writer = Code128Writer()
        val bitMatrix = writer.encode(text, BarcodeFormat.CODE_128, 300, 100)
        val width = bitMatrix.width
        val height = bitMatrix.height

        // 使用 KTX 扩展函数创建 Bitmap
        val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)

        // 批量设置像素（最高效的方式）
        val pixels = IntArray(width * height)
        for (x in 0 until width) {
            for (y in 0 until height) {
                pixels[y * width + x] = if (bitMatrix[x, y]) Color.Black.toArgb() else Color.White.toArgb()
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddOrderandParcelScreenPreview() {
    val navController = rememberNavController()
    AddOrderandParcelScreen(navController = navController)
}