package com.example.order_management.ui.screen

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.navigation.NavController
import com.example.order_management.ui.screen.OrderIdGenerator.generateOrderId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.tasks.await
import org.hashids.Hashids
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger


/* ----------------------------------------- Data Class ----------------------------------------- */
data class Parcel(
    val id: String = "",
    val description: String = "",
    val weight: String = "",
    val dimensions: String = "",
)

data class Order(
    val id: String = generateOrderId(),
    val senderId: String = "",
    val receiverId: String = "",
    val parcelIds: List<String> = emptyList(),
    val totalWeight: Double = 0.0,
    val cost: Double = 0.0
)

/* ----------------------------------- Save Data To Firestore ----------------------------------- */
fun saveParcelToFirestore(parcel: Parcel, onComplete: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("parcels").document()
    val parcelId = docRef.id
    val data = mapOf(
        "id" to parcelId,
        "description" to parcel.description,
        "weight" to parcel.weight,
        "dimensions" to parcel.dimensions
    )
    docRef.set(data).addOnSuccessListener { onComplete(parcelId) }
}

fun saveOrderToFirestore(order: Order) {
    val db = FirebaseFirestore.getInstance()
    val data = mapOf(
        "id" to order.id,
        "sender_id" to order.senderId,
        "receiver_id" to order.receiverId,
        "parcel_id" to order.parcelIds,
        "total_weight" to order.totalWeight,
        "cost" to order.cost
    )
    db.collection("orders").document(order.id).set(data)
}

/* --------------------------------------------- UI --------------------------------------------- */
@Composable
fun AddOrderScreen(
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
            // 订单ID + QR
            item { OrderInfoSection(order = order) }

            // Sender 选择
            item {
                CustomerSelector(
                    title = "寄件人",
                    onCustomerSelected = { customerId -> order = order.copy(senderId = customerId) }
                )
            }

            // Receiver 选择
            item {
                CustomerSelector(
                    title = "收件人",
                    onCustomerSelected = { customerId -> order = order.copy(receiverId = customerId) }
                )
            }

            // 包裹
            item {
                ParcelListSection(
                    parcels = order.parcelIds,
                    onAddParcel = { showParcelDialog = true },
                    onDeleteParcel = { index ->
                        order = order.copy(parcelIds = order.parcelIds.toMutableList().apply { removeAt(index) })
                    }
                )
            }

            // ✅ 汇总信息
            item { SummarySection(order = order) }

            // 提交按钮
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        saveOrderToFirestore(order)
                        navController.navigate("order") {
                            popUpTo("AddOrder") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = order.senderId.isNotEmpty() &&
                            order.receiverId.isNotEmpty() &&
                            order.parcelIds.isNotEmpty()
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
                saveParcelToFirestore(currentParcel) { parcelId ->
                    val weightNum = currentParcel.weight.toDoubleOrNull() ?: 0.0
                    val newTotalWeight = order.totalWeight + weightNum
                    val newCost = 5 + newTotalWeight * 2  // 运费公式
                    order = order.copy(
                        parcelIds = order.parcelIds + parcelId,
                        totalWeight = newTotalWeight,
                        cost = newCost
                    )
                    currentParcel = Parcel()
                    showParcelDialog = false
                }
            },
            onDismiss = {
                currentParcel = Parcel()
                showParcelDialog = false
            }
        )
    }
}

// -------------------- 订单信息 + QR --------------------
@Composable
fun OrderInfoSection(order: Order) {
    var showQRCode by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Order ID: ${order.id}", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "Show QR",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { showQRCode = !showQRCode }
                )
            }
            AnimatedVisibility(showQRCode) {
                val qrBitmap = generateQRCode(order.id)
                qrBitmap?.let {
                    Image(bitmap = it.asImageBitmap(), contentDescription = "QR", modifier = Modifier.size(120.dp))
                }
            }
        }
    }
}

@Composable
fun CustomerSelector(
    title: String,
    onCustomerSelected: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var customers by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }

    // Firestore 拉取客户列表
    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("customers").get().await()
            customers = snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: "未知用户"
                id to name
            }
            isLoading = false
            println("加载到 ${customers.size} 个客户: $customers") // 调试日志
        } catch (e: Exception) {
            errorMessage = "加载客户列表失败: ${e.message}"
            isLoading = false
            println("加载客户失败: ${e.message}") // 调试日志
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // 搜索输入框和下拉按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = selectedCustomer?.second ?: searchText,
                    onValueChange = {
                        if (selectedCustomer == null) {
                            searchText = it
                            expanded = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(
                            when {
                                isLoading -> "加载中..."
                                errorMessage != null -> "加载失败"
                                customers.isEmpty() -> "暂无客户数据"
                                else -> "选择或搜索客户"
                            }
                        )
                    },
                    enabled = !isLoading && customers.isNotEmpty() && errorMessage == null,
                    singleLine = true,
                    trailingIcon = {
                        Row {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            if (selectedCustomer != null) {
                                IconButton(
                                    onClick = {
                                        selectedCustomer = null
                                        searchText = ""
                                        expanded = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "清除选择",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 下拉按钮
                IconButton(
                    onClick = {
                        println("点击下拉按钮, customers: ${customers.size}, expanded: $expanded") // 调试
                        if (!isLoading && customers.isNotEmpty()) {
                            expanded = !expanded
                        }
                    },
                    enabled = !isLoading && customers.isNotEmpty() && errorMessage == null
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "收起" else "展开"
                    )
                }
            }

            // 客户列表
            AnimatedVisibility(
                visible = expanded && customers.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp) // 限制最大高度
                    ) {
                        // 过滤客户列表
                        val filteredCustomers = customers.filter { customer ->
                            customer.second.contains(searchText, ignoreCase = true) ||
                                    customer.first.contains(searchText, ignoreCase = true)
                        }

                        if (filteredCustomers.isEmpty() && searchText.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "未找到匹配的客户",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(filteredCustomers.size) { index ->
                                val customer = filteredCustomers[index]

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            println("选择客户: $customer") // 调试
                                            selectedCustomer = customer
                                            searchText = ""
                                            expanded = false
                                            onCustomerSelected(customer.first)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = customer.second,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "ID: ${customer.first}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (index < filteredCustomers.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 错误信息显示
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 调试信息（发布时删除）
            if (customers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "已加载 ${customers.size} 个客户 | 展开状态: $expanded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ParcelListSection(
    parcels: List<String>,
    onAddParcel: () -> Unit,
    onDeleteParcel: (Int) -> Unit = {} // Add this parameter with default empty implementation
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
                Text("包裹 (${parcels.size})", fontWeight = FontWeight.Bold)
                FloatingActionButton(onClick = onAddParcel, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "添加包裹")
                }
            }

            if (parcels.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "暂无包裹，请点击右上角按钮添加",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                parcels.forEachIndexed { index, id ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Parcel ID: $id")
                        // Optional: Add delete button
                        TextButton(onClick = { onDeleteParcel(index) }) {
                            Text("删除", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
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
                    label = { Text("尺寸") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = parcel.description.isNotEmpty()) {
                Text("添加")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

// -------------------- 汇总 --------------------
@Composable
fun SummarySection(order: Order) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("订单汇总", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("总重量: ${order.totalWeight} kg")
            Text("运费: RM ${"%.2f".format(order.cost)}")
        }
    }
}

object OrderIdGenerator {
    private val hashids = Hashids("Log-Express", 8, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")
    private val dateFormat = SimpleDateFormat("yyMMdd", Locale.ROOT)
    private val counter = AtomicInteger(0)
    @Volatile private var currentDate: String = dateFormat.format(Date())

    fun generateOrderId(): String {
        val today = dateFormat.format(Date())

        // 如果日期变了，重置 counter
        if (today != currentDate) {
            synchronized(this) {
                if (today != currentDate) {
                    currentDate = today
                    counter.set(0)
                }
            }
        }

        // 递增序列号
        val sequence = counter.getAndIncrement()

        // 用 Hashids 编码序列号
        val encoded = hashids.encode(sequence.toLong())

        return "ORD-$today$encoded"
    }
}

// -------------------- 生成 QR / Barcode --------------------
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
