package com.example.order_management.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldPath
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject


// ---------------- 数据类 ----------------
data class ParcelInfo(
    val id: String = "",
    val description: String = "",
    val weight: String = "",
    val dimensions: String = "",
    val value: String = ""
)
@Composable
fun OrderDetailScreen(orderId: String) {
    val db = FirebaseFirestore.getInstance()

    var senderName by remember { mutableStateOf("加载中...") }
    var receiverName by remember { mutableStateOf("加载中...") }
    var parcels by remember { mutableStateOf<List<ParcelInfo>>(emptyList()) }

    // 🔄 监听订单
    DisposableEffect(orderId) {
        var parcelListener: ListenerRegistration? = null

        val orderListener = db.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("获取订单失败: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val senderId = snapshot.getString("sender_id") ?: "未知"
                    val receiverId = snapshot.getString("receiver_id") ?: "未知"
                    val parcelIds = snapshot.get("parcel_ids") as? List<String> ?: emptyList()

                    // ✅ 查询 sender 名字
                    if (senderId != "未知") {
                        db.collection("customers")
                            .whereEqualTo("id", senderId)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { result ->
                                senderName = result.documents.firstOrNull()?.getString("name") ?: senderId
                            }
                    }

                    // ✅ 查询 receiver 名字
                    if (receiverId != "未知") {
                        db.collection("customers")
                            .whereEqualTo("id", receiverId)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { result ->
                                receiverName = result.documents.firstOrNull()?.getString("name") ?: receiverId
                            }
                    }

                    // 🔄 监听 parcels
                    parcelListener?.remove()
                    if (parcelIds.isNotEmpty()) {
                        parcelListener = db.collection("parcels")
                            .whereIn(FieldPath.documentId(), parcelIds)
                            .addSnapshotListener { result, err ->
                                if (err != null) {
                                    println("获取包裹失败: ${err.message}")
                                    return@addSnapshotListener
                                }
                                if (result != null) {
                                    parcels = result.documents.map { doc ->
                                        ParcelInfo(
                                            id = doc.id,
                                            description = doc.getString("description") ?: "",
                                            weight = doc.getString("weight") ?: "",
                                            dimensions = doc.getString("dimensions") ?: "",
                                            value = doc.getString("value") ?: ""
                                        )
                                    }
                                }
                            }
                    } else {
                        parcels = emptyList()
                    }
                }
            }

        onDispose {
            orderListener.remove()
            parcelListener?.remove()
        }
    }

    // ✅ 缓存 QR 生成，只在依赖变化时触发
    val qrCodeBitmap by remember(orderId, senderName, receiverName, parcels) {
        mutableStateOf(
            if (senderName != "加载中..." && receiverName != "加载中...") {
                generateQRCode(
                    JSONObject().apply {
                        put("orderId", orderId)
                        put("sender", senderName)
                        put("receiver", receiverName)
                        put("parcelCount", parcels.size)
                    }.toString()
                )
            } else {
                null
            }
        )
    }

    // ---------------- UI ----------------
    Column(modifier = Modifier.padding(16.dp)) {
        Text("订单详情: $orderId", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Text("寄件人: $senderName", fontSize = 14.sp)
        Text("收件人: $receiverName", fontSize = 14.sp)

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ 显示二维码
        qrCodeBitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "订单二维码",
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("包裹列表 (${parcels.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD05667))

        if (parcels.isEmpty()) {
            Text("暂无包裹", color = Color.Gray, modifier = Modifier.padding(8.dp))
        } else {
            parcels.forEachIndexed { index, parcel ->
                ParcelInfoItem(index = index + 1, parcel = parcel)
            }
        }
    }
}


@Composable
private fun ParcelInfoItem(index: Int, parcel: ParcelInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("$index. ${parcel.description}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (parcel.weight.isNotEmpty()) {
                Text("重量: ${parcel.weight} kg", fontSize = 12.sp, color = Color.Gray)
            }
            if (parcel.dimensions.isNotEmpty()) {
                Text("尺寸: ${parcel.dimensions}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

/* ------------------------------------ Generate QR / Barcode ----------------------------------- */
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