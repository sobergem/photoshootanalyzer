package com.sobergem.photoshootanalyser.presentation.screen

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sobergem.photoshootanalyser.presentation.screen.uistate.PhotographyUiState
import java.nio.ByteBuffer
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale

@Composable
fun PhotographyScreen(
    viewModel: PhotographyViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    var hasCameraPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        }
    )
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        if (uiState.value is PhotographyUiState.Success && capturedBitmap != null) {
            Image(
                bitmap = capturedBitmap!!.asImageBitmap(),
                contentDescription = "Captured Analysis",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Add a dark overlay to make text readable
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
        }
        when (val state = uiState.value) {
            is PhotographyUiState.Idle -> {
                if(hasCameraPermission){
                    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
                    val imageCapture = remember { ImageCapture.Builder().build() }
                    AndroidView(
                        factory = { context ->
                            PreviewView(context).apply{
                                scaleType = PreviewView.ScaleType.FIT_CENTER
                            }
                        },
                        update = { previewView ->
                            val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(previewView.context)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = androidx.camera.core.Preview.Builder().build().also{
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture
                                    )
                                } catch (exc: Exception) {
                                    Log.e(
                                        "PhotographyScreen",
                                        "Use case binding failed",
                                        exc
                                    )
                                }
                            }, ContextCompat.getMainExecutor(previewView.context))
                        }
                    )
                    Button(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
                        onClick = {
                       captureImageAndAnalyze(
                           imageCapture = imageCapture,
                           executor = ContextCompat.getMainExecutor(context),
                           onError = { exception ->
                               Log.e("PhotographyScreen", "Capture failed", exception)
                           },
                           onImageCaptured = {bytes, bitmap ->
                               capturedBitmap = bitmap
                               viewModel.analyzeFrame(bytes)
                           }
                       )
                    }){
                        Text("Analyze Scene")
                    }
                }else{
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text("We need camera access to analyze your scene")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }){
                            Text("Grant Camera Permission")
                        }
                    }
                }
            }

            is PhotographyUiState.Loading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Gemini is reading the composition...")
                }
            }

            is PhotographyUiState.Success -> {
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) +
                            androidx.compose.animation.fadeIn(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    androidx.compose.material3.Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.5f)
                            .navigationBarsPadding(), // Ensures it stays above nav keys
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        tonalElevation = 8.dp // Gives the "floating" effect
                    ){
                        LazyColumn(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text("Composition Analysis", style = MaterialTheme.typography.headlineSmall)
                                Text("Detected Style: ${state.blueprint.styleDetected}", style = MaterialTheme.typography.bodyLarge)
                            }

                            // 2. Camera Settings Card (Grouped Hardware Data)
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Recommended Settings", style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        SettingRow("Aperture", state.blueprint.recommendedSettings.aperture)
                                        SettingRow("Shutter Speed", state.blueprint.recommendedSettings.shutterSpeed)
                                        SettingRow("ISO", state.blueprint.recommendedSettings.iso)
                                    }
                                }
                            }

                            // 3. Stylist Tips Section
                            item {
                                Text("Pro Advice", style = MaterialTheme.typography.titleMedium)
                                // Fix: joinToString() creates a clean, formatted string
                                Text(
                                    text = state.blueprint.stylistTips.joinToString(separator = "\n• ", prefix = "• "),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        // 1. Clear the locally saved image so it doesn't flash next time
                                        capturedBitmap = null
                                        // 2. Tell the ViewModel to go back to the camera preview
                                        viewModel.resetToIdle()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Done")
                                }
                            }
                        }
                    }
                }
            }

            is PhotographyUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    Text(
                        text = "Oops! The server is busy.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // The Retry Button
                    Button(onClick = { viewModel.reRunAnalysis() }) {
                        Text("Retry Analysis")
                    }
                }
            }
        }
    }
}

private fun captureImageAndAnalyze(
    imageCapture: ImageCapture,
    executor: java.util.concurrent.Executor,
    onImageCaptured: (ByteArray, Bitmap) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                // 1. Extract the raw image bytes from the hardware buffer
                val buffer: ByteBuffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                // 2. VERY IMPORTANT: Close the image proxy to free up hardware memory!
                image.close()

                // 3. Dispatch the bytes upstream
                onImageCaptured(bytes, bitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

@Composable
fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}