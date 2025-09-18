package com.example.core_ui.components

import android.Manifest
import android.util.Log
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // 一进入页面就请求权限
    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // 已授权 → 显示扫码 UI
            permissionState.status.isGranted -> {
                ScanUi()
            }
            // 未授权 → 返回上一页
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
fun ScanUi() {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }

    var selectedTab by remember { mutableStateOf("Scan") }

    Box(modifier = Modifier.fillMaxSize()) {
        // ---------- 相机预览 ----------
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
                                Log.d("ScanResult", "Scanned Code: $code")
                            }
                        }
                    )
                    cameraController.bindToLifecycle(lifecycleOwner)
                    this.controller = cameraController
                }
            }
        )

        // ---------- 扫描框 ----------
        ScannerOverlay(
            modifier = Modifier.matchParentSize()
        )

        // ---------- 底部按钮区 ----------
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scan From Gallery
            Button(
                onClick = { /* TODO: 从图库选取 */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF69B4),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text("Scan From Gallery")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Segmented control
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color(0xFFEEEEEE), RoundedCornerShape(50))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("In-Stock", "Out-Stock", "Transfer").forEach { tab ->
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
        val right = left + framePx
        val bottom = top + framePx

        // 边框
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