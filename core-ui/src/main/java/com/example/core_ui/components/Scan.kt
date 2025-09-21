package com.example.core_ui.components

import android.Manifest
import android.util.Log
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // 请求相机权限
    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            permissionState.status.isGranted -> {
                ScanUi(navController = navController)
            }
            else -> {
                LaunchedEffect(permissionState.status) {
                    if (!permissionState.status.isGranted) {
                        backDispatcher?.onBackPressed()
                    }
                }
            }
        }
    }
}

@Composable
fun ScanUi(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }

    var selectedTab by remember { mutableStateOf("In-Stock") }
    var lastScannedCode by remember { mutableStateOf("") }
    var lastScanTime by remember { mutableStateOf(0L) }
    val scanCooldownMs = 3000L

    Box(modifier = Modifier.fillMaxSize()) {
        // 相机预览
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_CODE_128,
                            Barcode.FORMAT_EAN_13
                        ).build()
                    val scanner = BarcodeScanning.getClient(options)
                    cameraController.setImageAnalysisAnalyzer(
                        ContextCompat.getMainExecutor(ctx),
                        MlKitAnalyzer(
                            listOf(scanner),
                            ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                            ContextCompat.getMainExecutor(ctx)
                        ) { result ->
                            val barcodeResults = result?.getValue(scanner)
                            if (!barcodeResults.isNullOrEmpty()) {
                                val code = barcodeResults.first().rawValue
                                val currentTime = System.currentTimeMillis()
                                if (code != null &&
                                    (code != lastScannedCode || currentTime - lastScanTime > scanCooldownMs)) {
                                    lastScannedCode = code
                                    lastScanTime = currentTime
                                    navigateWithQr(code, navController, selectedTab)
                                }
                            }
                        }
                    )
                    cameraController.bindToLifecycle(lifecycleOwner)
                    this.controller = cameraController
                }
            }
        )

        // 扫描框
        ScannerOverlay(modifier = Modifier.matchParentSize())

        // 扫描提示文字
        Text(
            text = "Try to Scan a QR Code",
            color = Color.Green,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-160).dp)
        )

        // 底部切换按钮
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color(0xFFEEEEEE), RoundedCornerShape(50))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("In-Stock", "Out-Stock").forEach { tab ->
                    Button(
                        onClick = { selectedTab = tab },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == tab) Color(0xFFFFB6C1) else Color.White,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(tab)
                    }
                }
            }
        }
    }
}

private fun navigateWithQr(code: String, navController: NavController, selectedTab: String) {
    try {
        val json = JSONObject(code)
        val orderId = json.getString("orderId")
        val sender = json.getString("sender")
        val receiver = json.getString("receiver")
        val parcelCount = json.getInt("parcelCount")

        if (selectedTab == "In-Stock") {
            val route = "inStock/$orderId/$sender/$receiver/$parcelCount"
            navController.navigate(route)
        } else {
            val rackName = json.optString("rack", "")
            val route = "outStock/$orderId/$sender/$receiver/$parcelCount/$rackName"
            navController.navigate(route)
        }
    } catch (e: Exception) {
        Log.e("ScanResult", "QR code parsing failed: ${e.message}")
        Log.e("ScanResult", "Raw QR code content: $code")
    }
}

@Composable
fun ScannerOverlay(modifier: Modifier = Modifier) {
    val frameDp = 250.dp
    val cornerDp = 16.dp
    val lineHeightDp = 3.dp

    val transition = rememberInfiniteTransition()
    val frac by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier) {
        val framePx = frameDp.toPx()
        val cornerPx = cornerDp.toPx()
        val lineHeightPx = lineHeightDp.toPx()

        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val left = centerX - framePx / 2f
        val top = centerY - framePx / 2f
        val bottom = top + framePx

        // 框
        drawRoundRect(
            color = Color(0xFFFF69B4),
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(framePx, framePx),
            cornerRadius = CornerRadius(cornerPx, cornerPx),
            style = Stroke(width = 6f)
        )

        // 扫描线
        val maxLineY = bottom - lineHeightPx
        val lineY = top + frac * (maxLineY - top)
        drawRect(
            color = Color.Green,
            topLeft = Offset(left, lineY),
            size = androidx.compose.ui.geometry.Size(framePx, lineHeightPx)
        )
    }
}