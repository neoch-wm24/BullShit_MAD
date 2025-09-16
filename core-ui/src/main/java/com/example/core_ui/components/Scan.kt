package com.example.core_ui.components

import android.Manifest
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalPermissionsApi::class) // Opt-in to use experimental permissions API
@Composable
fun ScanScreen(modifier: Modifier = Modifier) {

    var barcode by rememberSaveable { mutableStateOf<String?>("No Code Scanned") }
    val permissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )
    var oncancel by remember(permissionState.status.shouldShowRationale) {
        mutableStateOf(permissionState.status.shouldShowRationale)
    }
    if (barcode != null) {

        Box(
            modifier = Modifier
                .fillMaxSize() // Make the Box take up the entire screen
                .background(Color.LightGray) // Optional: Add a background color for visibility
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (oncancel) {
                    ShowRationaleDialog(
                        onDismiss = { oncancel = false },
                        onConfirm = { permissionState.launchPermissionRequest() },
                        body = permissionState.permission
                    )
                }

                // Determine the text to show based on the permission state
                val textToShow = if (permissionState.status.shouldShowRationale) {
                    // If the user has denied the permission but the rationale can be shown,
                    // explain why the app requires this permission
                    "The Camera permission is important for this app. Please grant the permission."
                } else if (!permissionState.status.isGranted) {
                    // If it's the first time the user lands on this feature, or the user
                    // doesn't want to be asked again for this permission, explain that the
                    // permission is required
                    "Camera permission required for this feature to be available. Please grant the permission."
                } else {
                    // If permission is granted, show the scanned barcode or a default message
                    barcode ?: "No Scanned"
                }

                // Display the determined text
                Text(
                    text = textToShow,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )

                // Show a button to scan a QR or barcode if permission is granted
                if (permissionState.status.isGranted) {
                    Button(onClick = { barcode = null }) {
                        Text("Scan QR or Barcode")
                    }
                } else {
                    // Show a button to request camera permission if not granted
                    Button(onClick = { permissionState.launchPermissionRequest() }) {
                        Text("Request permission")
                    }
                }
            }
        }
    } else {
        // If no barcode has been scanned, show the QR/barcode scanner
        ScanCode(onQrCodeDetected = {
            barcode = it // Update the barcode state with the scanned value
        })
    }

}
@Composable
fun ScanCode(
    onQrCodeDetected: (String) -> Unit, // Callback to handle detected QR/barcode
    modifier: Modifier = Modifier
) {
    // State to hold the detected barcode value
    var barcode by remember { mutableStateOf<String?>(null) }

    // Get the current context and lifecycle owner for camera operations
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // State to track if a QR/barcode has been detected
    var qrCodeDetected by remember { mutableStateOf(false) }

    // State to hold the bounding rectangle of the detected barcode
    var boundingRect by remember { mutableStateOf<Rect?>(null) }

    // Initialize the camera controller with the current context
    val cameraController = remember {
        LifecycleCameraController(context)
    }

    // AndroidView to integrate the camera preview and barcode scanning
    AndroidView(
        modifier = modifier.fillMaxSize(), // Make the view take up the entire screen
        factory = { ctx ->
            PreviewView(ctx).apply {
                // Configure barcode scanning options for supported formats
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_CODABAR,
                        Barcode.FORMAT_CODE_93,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_AZTEC
                    )
                    .build()

                // Initialize the barcode scanner client with the configured options
                val barcodeScanner = BarcodeScanning.getClient(options)

                // Set up the image analysis analyzer for barcode detection
                cameraController.setImageAnalysisAnalyzer(
                    ContextCompat.getMainExecutor(ctx), // Use the main executor
                    MlKitAnalyzer(
                        listOf(barcodeScanner), // Pass the barcode scanner
                        ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED, // Use view-referenced coordinates
                        ContextCompat.getMainExecutor(ctx) // Use the main executor
                    ) { result: MlKitAnalyzer.Result? ->
                        // Process the barcode scanning results
                        val barcodeResults = result?.getValue(barcodeScanner)
                        if (!barcodeResults.isNullOrEmpty()) {
                            // Update the barcode state with the first detected barcode
                            barcode = barcodeResults.first().rawValue

                            // Update the state to indicate a barcode has been detected
                            qrCodeDetected = true

                            // Update the bounding rectangle of the detected barcode
                            boundingRect = barcodeResults.first().boundingBox

                            // Log the bounding box for debugging purposes
                            Log.d("Looking for Barcode ", barcodeResults.first().boundingBox.toString())
                        }
                    }
                )

                // Bind the camera controller to the lifecycle owner
                cameraController.bindToLifecycle(lifecycleOwner)

                // Set the camera controller for the PreviewView
                this.controller = cameraController
            }
        }
    )

    // If a QR/barcode has been detected, trigger the callback
    if (qrCodeDetected) {
        LaunchedEffect(Unit) {
            // Delay for a short duration to allow recomposition
            delay(100) // Adjust delay as needed

            // Call the callback with the detected barcode value
            onQrCodeDetected(barcode ?: "")
        }

        // Draw a rectangle around the detected barcode
        DrawRectangle(rect = boundingRect)
    }
}

@Composable
fun DrawRectangle(rect: Rect?) {
    // Convert the Android Rect to a Compose Rect
    val composeRect = rect?.toComposeRect()

    // Draw the rectangle on a Canvas if the rect is not null
    composeRect?.let {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = Color.Red,
                topLeft = Offset(it.left, it.top), // Set the top-left position
                size = Size(it.width, it.height), // Set the size of the rectangle
                style = Stroke(width = 5f) // Use a stroke style with a width of 5f
            )
        }
    }
}
@Composable
fun ShowRationaleDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title:String="Permission",
    body:String="Permission needed"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = body)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
