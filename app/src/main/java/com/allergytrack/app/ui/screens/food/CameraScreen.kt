package com.allergytrack.app.ui.screens.food

import android.content.Context
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.ui.navigation.Screen
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: FoodLogViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    // Naviguj při úspěšném výsledku
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is FoodLogUiState.RecognitionResult -> {
                val food = state.foods.firstOrNull()
                if (food != null) {
                    val allergens = food.allergens.joinToString("|")
                    navController.navigate(
                        Screen.FoodConfirm.createRoute(food.name, allergens)
                    ) {
                        popUpTo(Screen.Camera.route) { inclusive = true }
                    }
                }
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Camera preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                setupCamera(ctx, lifecycleOwner, previewView) { capture ->
                    imageCapture = capture
                }
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading overlay
        if (uiState is FoodLogUiState.Processing) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Text("Rozpoznávám jídlo...", color = Color.White)
                }
            }
        }

        // Viewfinder corners overlay
        CameraViewfinder()

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zpět", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Vyfoť jídlo",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp)) // balance
        }

        // Bottom controls
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .navigationBarsPadding()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Capture button
            IconButton(
                onClick = {
                    imageCapture?.let { capture ->
                        takePhoto(context, capture, ContextCompat.getMainExecutor(context)) { uri ->
                            viewModel.onImageCaptured(uri)
                        }
                    }
                },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "Vyfotit",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun CameraViewfinder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
        ) {
            val cornerColor = Color.White
            val cornerSize = 24.dp
            val strokeWidth = 3.dp

            // Corners drawn via Box borders
            Box(
                Modifier
                    .size(cornerSize)
                    .align(Alignment.TopStart)
                    .border(
                        width = strokeWidth,
                        color = cornerColor,
                        shape = RoundedCornerShape(topStart = 8.dp)
                    )
            )
            Box(
                Modifier
                    .size(cornerSize)
                    .align(Alignment.TopEnd)
                    .border(
                        width = strokeWidth,
                        color = cornerColor,
                        shape = RoundedCornerShape(topEnd = 8.dp)
                    )
            )
            Box(
                Modifier
                    .size(cornerSize)
                    .align(Alignment.BottomStart)
                    .border(
                        width = strokeWidth,
                        color = cornerColor,
                        shape = RoundedCornerShape(bottomStart = 8.dp)
                    )
            )
            Box(
                Modifier
                    .size(cornerSize)
                    .align(Alignment.BottomEnd)
                    .border(
                        width = strokeWidth,
                        color = cornerColor,
                        shape = RoundedCornerShape(bottomEnd = 8.dp)
                    )
            )
        }
    }
}

private fun setupCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
            onImageCaptureReady(imageCapture)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onSuccess: (Uri) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg"
    )
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onSuccess(Uri.fromFile(photoFile))
            }
            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }
        }
    )
}
