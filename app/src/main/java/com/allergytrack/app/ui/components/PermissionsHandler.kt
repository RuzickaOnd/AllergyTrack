package com.allergytrack.app.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

@Composable
fun RequestCameraPermission(
    onGranted: @Composable () -> Unit,
    onDenied: @Composable () -> Unit
) {
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        permissionRequested = true
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    when {
        permissionGranted -> onGranted()
        permissionRequested && !permissionGranted -> onDenied()
        else -> { /* Čekáme na odpověď */ }
    }
}
