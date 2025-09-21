package com.example.core_ui.components

import android.Manifest
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

    // Request camera permission when entering the page
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanUi(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }

    var selectedTab by remember { mutableStateOf("In-Stock") }
    var lastScannedCode by remember { mutableStateOf("") }
    var lastScanTime by remember { mutableStateOf(0L) }
    val scanCooldownMs = 3000L // 3 second cooldown between scans

    // Gallery permission state
    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val galleryPermissionState = rememberPermissionState(galleryPermission)

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("ScanResult", "Selected image URI: $it")
            handleGalleryResult(it, navController, selectedTab, context)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
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

                                // Prevent duplicate scans with cooldown
                                if (code != null &&
                                    (code != lastScannedCode || currentTime - lastScanTime > scanCooldownMs)) {

                                    Log.d("ScanResult", "New scan detected: $code")
                                    lastScannedCode = code
                                    lastScanTime = currentTime

                                    navigateWithQr(code, navController, selectedTab)
                                } else {
                                    Log.d("ScanResult", "Duplicate scan ignored or within cooldown period")
                                }
                            }
                        }
                    )
                    cameraController.bindToLifecycle(lifecycleOwner)
                    this.controller = cameraController
                }
            }
        )

        // Scanner Overlay
        ScannerOverlay(modifier = Modifier.matchParentSize())

        // Bottom Button Area
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scan From Gallery Button
            Button(
                onClick = {
                    Log.d("ScanResult", "Gallery button clicked")

                    // For Android 10+ (API 29+), we don't need storage permission for gallery access
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Android 10+ - No storage permission needed for gallery
                        Log.d("ScanResult", "Android 10+, launching gallery directly")
                        galleryLauncher.launch("image/*")
                    } else {
                        // Android 9 and below - Need storage permission
                        if (galleryPermissionState.status.isGranted) {
                            Log.d("ScanResult", "Storage permission granted, launching gallery")
                            galleryLauncher.launch("image/*")
                        } else {
                            Log.d("ScanResult", "Requesting storage permission")
                            galleryPermissionState.launchPermissionRequest()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF69B4),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text("Scan From Gallery")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Segmented control for In-Stock/Out-Stock
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color(0xFFEEEEEE), RoundedCornerShape(50))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("In-Stock", "Out-Stock").forEach { tab ->
                    Button(
                        onClick = {
                            selectedTab = tab
                            Log.d("ScanResult", "Selected tab: $tab")
                        },
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

    // Handle gallery permission result for Android 9 and below
    LaunchedEffect(galleryPermissionState.status) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            galleryPermissionState.status.isGranted) {
            Log.d("ScanResult", "Storage permission granted, launching gallery")
            galleryLauncher.launch("image/*")
        }
    }
}

// Helper Functions

private fun navigateWithQr(code: String, navController: NavController, selectedTab: String) {
    try {
        Log.d("ScanResult", "Parsing QR code: $code")
        val json = JSONObject(code)
        val orderId = json.getString("orderId")
        val sender = json.getString("sender")
        val receiver = json.getString("receiver")
        val parcelCount = json.getInt("parcelCount")

        Log.d("ScanResult", "Parsed - OrderID: $orderId, Sender: $sender, Receiver: $receiver, ParcelCount: $parcelCount")

        if (selectedTab == "In-Stock") {
            val route = "inStock/$orderId/$sender/$receiver/$parcelCount"
            Log.d("ScanResult", "Navigating to In-Stock: $route")
            navController.navigate(route)
        } else {
            val rackName = json.optString("rack", "")
            val route = "outStock/$orderId/$sender/$receiver/$parcelCount/$rackName"
            Log.d("ScanResult", "Navigating to Out-Stock: $route")
            navController.navigate(route)
        }
    } catch (e: Exception) {
        Log.e("ScanResult", "QR code parsing failed: ${e.message}")
        Log.e("ScanResult", "Raw QR code content: $code")
    }
}

private fun handleGalleryResult(
    uri: Uri,
    navController: NavController,
    selectedTab: String,
    context: android.content.Context
) {
    try {
        Log.d("ScanResult", "Processing gallery image: $uri")
        val image = InputImage.fromFilePath(context, uri)

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_EAN_13
            ).build()

        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                Log.d("ScanResult", "Gallery scan success, found ${barcodes.size} barcodes")
                if (barcodes.isNotEmpty()) {
                    val code = barcodes.first().rawValue
                    Log.d("ScanResult", "Scanned Code from Gallery: $code")
                    code?.let { navigateWithQr(it, navController, selectedTab) }
                } else {
                    Log.w("ScanResult", "No QR codes found in selected image")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ScanResult", "Gallery scan failed: ${exception.message}")
                exception.printStackTrace()
            }
    } catch (e: Exception) {
        Log.e("ScanResult", "Gallery image processing error: ${e.message}")
        e.printStackTrace()
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

        // Scanner frame border
        drawRoundRect(
            color = Color(0xFFFF69B4),
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(framePx, framePx),
            cornerRadius = CornerRadius(cornerPx, cornerPx),
            style = Stroke(width = 6f)
        )

        // Animated scanning line
        val maxLineY = bottom - lineHeightPx
        val lineY = top + frac * (maxLineY - top)

        drawRect(
            color = Color.Green,
            topLeft = Offset(left, lineY),
            size = androidx.compose.ui.geometry.Size(framePx, lineHeightPx)
        )
    }
}